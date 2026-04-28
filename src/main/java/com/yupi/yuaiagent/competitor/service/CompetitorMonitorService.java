package com.yupi.yuaiagent.competitor.service;

import com.yupi.yuaiagent.competitor.entity.AppMonitoringConfig;
import com.yupi.yuaiagent.competitor.entity.CompetitorReview;

import java.util.List;

/**
 * 竞品监控编排服务
 */
public interface CompetitorMonitorService {

    /**
     * 执行完整的竞品监控流程（采集→分析→报告→预警）
     */
    void executeDailyMonitor();

    /**
     * 抓取指定App的评论
     */
    List<CompetitorReview> fetchAppReviews(AppMonitoringConfig config, int maxPages);

    /**
     * 手动触发分析
     */
    String triggerManualAnalysis(List<String> appIds);
}
