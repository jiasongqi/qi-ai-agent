package com.yupi.yuaiagent.competitor.service.impl;

import com.yupi.yuaiagent.competitor.agent.CompetitorAnalysisAgent;
import com.yupi.yuaiagent.competitor.agent.CompetitorAnalysisAgentFactory;
import com.yupi.yuaiagent.competitor.crawler.AppStoreReviewCrawler;
import com.yupi.yuaiagent.competitor.crawler.AppStoreUpdateLogCrawler;
import com.yupi.yuaiagent.competitor.crawler.OfficialSiteMonitor;
import com.yupi.yuaiagent.competitor.entity.AppMonitoringConfig;
import com.yupi.yuaiagent.competitor.entity.CompetitorAnalysisReport;
import com.yupi.yuaiagent.competitor.entity.CompetitorPolicyChange;
import com.yupi.yuaiagent.competitor.entity.CompetitorReview;
import com.yupi.yuaiagent.competitor.mapper.CompetitorAnalysisReportMapper;
import com.yupi.yuaiagent.competitor.mapper.CompetitorReviewMapper;
import com.yupi.yuaiagent.competitor.service.AlertService;
import com.yupi.yuaiagent.competitor.service.AppMonitoringConfigService;
import com.yupi.yuaiagent.competitor.service.CompetitorMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompetitorMonitorServiceImpl implements CompetitorMonitorService {

    @Autowired
    private AppMonitoringConfigService configService;

    @Autowired
    private AppStoreReviewCrawler reviewCrawler;

    @Autowired
    private AppStoreUpdateLogCrawler updateLogCrawler;

    @Autowired
    private OfficialSiteMonitor officialSiteMonitor;

    @Autowired
    private CompetitorAnalysisAgentFactory agentFactory;

    @Autowired
    private CompetitorReviewMapper reviewMapper;

    @Autowired
    private CompetitorAnalysisReportMapper reportMapper;

    @Autowired
    private AlertService alertService;

    @Override
    public void executeDailyMonitor() {
        log.info("============= 开始执行每日竞品监控任务 =============");
        List<AppMonitoringConfig> activeConfigs = configService.listActiveMonitors();
        if (activeConfigs.isEmpty()) {
            log.warn("没有活跃的监控配置，跳过执行");
            return;
        }

        // 1. 数据采集阶段
        List<CompetitorReview> allNewReviews = new ArrayList<>();
        Map<String, String> appNameMap = new HashMap<>();
        StringBuilder contextBuilder = new StringBuilder();

        for (AppMonitoringConfig config : activeConfigs) {
            appNameMap.put(config.getAppId(), config.getAppName());
            try {
                // 1a. 抓取评论
                log.info("正在抓取 [{}] 的评论", config.getAppName());
                List<CompetitorReview> reviews = fetchAppReviews(config, 5);
                allNewReviews.addAll(reviews);
                log.info("[{}] 成功抓取 {} 条评论", config.getAppName(), reviews.size());

                // 1b. 抓取更新日志
                try {
                    var changelog = updateLogCrawler.fetchLatestChangelog(config.getAppId());
                    if (changelog != null && changelog.getDiffContent() != null) {
                        contextBuilder.append(String.format("[%s] 版本 %s 更新: %s\n",
                                config.getAppName(), changelog.getVersion(), changelog.getChangelogContent()));
                    }
                } catch (Exception e) {
                    log.debug("抓取 [{}] 更新日志失败: {}", config.getAppName(), e.getMessage());
                }

                // 1c. 监控官网条款变化（如有配置 URL）
                if (config.getPolicyUrl() != null && !config.getPolicyUrl().isEmpty()) {
                    try {
                        CompetitorPolicyChange change = officialSiteMonitor.monitorPolicyChange(
                                config.getAppId(), config.getPolicyUrl(), "terms");
                        if (change != null) {
                            contextBuilder.append(String.format("[%s] 条款变化检测: %s\n",
                                    config.getAppName(), change.getDiffContent()));
                        }
                    } catch (Exception e) {
                        log.debug("监控 [{}] 官网条款失败: {}", config.getAppName(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("抓取 [{}] 数据失败: {}", config.getAppName(), e.getMessage());
            }
        }

        // 2. LLM 分析阶段
        if (!allNewReviews.isEmpty() || contextBuilder.length() > 0) {
            log.info("开始 LLM 归因分析，评论数={}, 补充上下文长度={}", allNewReviews.size(), contextBuilder.length());
            try {
                String analysisResult = runAnalysis(allNewReviews, appNameMap, contextBuilder.toString());

                // 3. 报告持久化
                CompetitorAnalysisReport report = new CompetitorAnalysisReport();
                report.setReportDate(LocalDate.now());
                report.setReportType("daily_monitor");
                report.setTargetApps(String.join(",", appNameMap.values()));
                report.setFullReport(analysisResult);
                report.setGeneratedBy("CompetitorAnalysisAgent");
                reportMapper.insert(report);
                log.info("分析报告已持久化，reportId={}", report.getId());

                // 4. 预警生成（基于风险权重和分析结果）
                generateAlertsFromConfig(activeConfigs, analysisResult);
            } catch (Exception e) {
                log.error("LLM 分析或报告持久化失败: {}", e.getMessage());
                alertService.createAlert("system", null, "red",
                        "每日监控分析失败", "LLM 归因分析异常: " + e.getMessage(), null);
            }
        } else {
            log.info("本期无新增数据，跳过分析");
        }

        log.info("============= 每日竞品监控任务执行完毕 =============");
    }

    @Override
    public List<CompetitorReview> fetchAppReviews(AppMonitoringConfig config, int maxPages) {
        List<CompetitorReview> reviews = reviewCrawler.fetchReviews(config.getAppId(), maxPages);
        if (!reviews.isEmpty()) {
            for (CompetitorReview review : reviews) {
                review.setAppId(config.getAppId());
            }
            saveReviews(reviews);
        }
        return reviews;
    }

    @Override
    public String triggerManualAnalysis(List<String> appIds) {
        List<AppMonitoringConfig> configs = configService.listActiveMonitors().stream()
                .filter(c -> appIds.contains(c.getAppId()))
                .collect(Collectors.toList());

        List<CompetitorReview> allReviews = new ArrayList<>();
        Map<String, String> appNameMap = new HashMap<>();
        StringBuilder contextBuilder = new StringBuilder();

        for (AppMonitoringConfig config : configs) {
            appNameMap.put(config.getAppId(), config.getAppName());
            List<CompetitorReview> reviews = fetchAppReviews(config, 3);
            allReviews.addAll(reviews);
        }

        // 执行 LLM 分析
        String analysisResult = runAnalysis(allReviews, appNameMap, contextBuilder.toString());

        // 报告持久化
        CompetitorAnalysisReport report = new CompetitorAnalysisReport();
        report.setReportDate(LocalDate.now());
        report.setReportType("manual");
        report.setTargetApps(String.join(",", appNameMap.values()));
        report.setFullReport(analysisResult);
        report.setGeneratedBy("CompetitorAnalysisAgent");
        reportMapper.insert(report);

        return analysisResult;
    }

    /**
     * 执行 LLM 归因分析（并发安全，每次创建新 Agent 实例）
     */
    private String runAnalysis(List<CompetitorReview> reviews, Map<String, String> appNameMap, String context) {
        CompetitorAnalysisAgent agent = agentFactory.create();
        return agent.analyze(reviews, appNameMap, context);
    }

    private void saveReviews(List<CompetitorReview> reviews) {
        for (CompetitorReview review : reviews) {
            try {
                reviewMapper.insert(review);
            } catch (Exception e) {
                log.debug("评论已存在或插入失败，reviewId={}", review.getReviewId());
            }
        }
    }

    /**
     * 基于 App 配置的风险权重生成预警
     */
    private void generateAlertsFromConfig(List<AppMonitoringConfig> configs, String analysisResult) {
        for (AppMonitoringConfig config : configs) {
            if (config.getRiskWeight() != null && config.getRiskWeight().doubleValue() < -0.5) {
                alertService.createAlert(
                        "risk_weight", config.getAppId(), "orange",
                        String.format("高风险App检测: %s", config.getAppName()),
                        String.format("App [%s] 风险权重 %.2f，分类: %s/%s，请关注相关用户申请。",
                                config.getAppName(), config.getRiskWeight(), config.getCategory(), config.getSubCategory()),
                        null
                );
            }
        }
    }
}
