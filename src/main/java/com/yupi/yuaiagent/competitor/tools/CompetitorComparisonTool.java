package com.yupi.yuaiagent.competitor.tools;

import com.yupi.yuaiagent.competitor.entity.AppMonitoringConfig;
import com.yupi.yuaiagent.competitor.entity.CompetitorReview;
import com.yupi.yuaiagent.competitor.mapper.CompetitorReviewMapper;
import com.yupi.yuaiagent.competitor.service.AppMonitoringConfigService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 竞品对比工具
 * 支持多App横向对比分析
 */
@Component
public class CompetitorComparisonTool {

    @Autowired
    private CompetitorReviewMapper reviewMapper;

    @Autowired
    private AppMonitoringConfigService configService;

    @Tool(description = "对比多个竞品的平均评分和评论数量")
    public String compareCompetitorsByRating(
            @ToolParam(description = "App ID列表，用逗号分隔，如 '1435044790,1462715669'") String appIds) {

        List<String> idList = Arrays.asList(appIds.split(","));
        StringBuilder sb = new StringBuilder();
        sb.append("# 竞品评分对比\n\n");
        sb.append("| App名称 | App ID | 评论总数 | 平均评分 |\n");
        sb.append("|---------|--------|----------|----------|\n");

        for (String appId : idList) {
            appId = appId.trim();
            List<CompetitorReview> reviews = reviewMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CompetitorReview>()
                            .eq(CompetitorReview::getAppId, appId)
            );

            String appName = getAppName(appId);
            int total = reviews.size();
            double avgRating = reviews.stream()
                    .filter(r -> r.getRating() != null)
                    .mapToInt(CompetitorReview::getRating)
                    .average().orElse(0);

            sb.append(String.format("| %s | %s | %d | %.1f |\n", appName, appId, total, avgRating));
        }

        return sb.toString();
    }

    @Tool(description = "分析指定App的差评主题分布（通过关键词统计）")
    public String analyzeNegativeThemes(
            @ToolParam(description = "App的iOS ID") String appId) {

        List<CompetitorReview> negativeReviews = reviewMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CompetitorReview>()
                        .eq(CompetitorReview::getAppId, appId)
                        .le(CompetitorReview::getRating, 2)
        );

        if (negativeReviews.isEmpty()) {
            return String.format("App ID %s 暂无差评数据（1-2星）。", appId);
        }

        // 简单关键词统计（印尼语常见投诉词）
        Map<String, Long> themeCount = negativeReviews.stream()
                .map(r -> r.getReviewContent() != null ? r.getReviewContent().toLowerCase() : "")
                .flatMap(content -> {
                    List<String> themes = new java.util.ArrayList<>();
                    if (content.contains("lambat") || content.contains("slow") || content.contains("lama")) themes.add("放款慢");
                    if (content.contains("bunga") || content.contains("interest") || content.contains("mahal")) themes.add("利息高");
                    if (content.contains("dc") || content.contains("debt collector") || content.contains("tagih")) themes.add("催收问题");
                    if (content.contains("penolakan") || content.contains("ditolak") || content.contains("rejected")) themes.add("拒贷");
                    if (content.contains("bug") || content.contains("error") || content.contains("crash")) themes.add("技术故障");
                    if (content.contains("penipuan") || content.contains("scam") || content.contains("tipu")) themes.add("欺诈怀疑");
                    if (content.contains("data") || content.contains("privasi") || content.contains("privacy")) themes.add("隐私问题");
                    if (themes.isEmpty()) themes.add("其他");
                    return themes.stream();
                })
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        String appName = getAppName(appId);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("# %s (%s) 差评主题分析\n\n", appName, appId));
        sb.append(String.format("差评总数: %d\n\n", negativeReviews.size()));
        sb.append("主题分布:\n");

        themeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    double pct = 100.0 * entry.getValue() / negativeReviews.size();
                    sb.append(String.format("- %s: %d条 (%.1f%%)\n", entry.getKey(), entry.getValue(), pct));
                });

        return sb.toString();
    }

    private String getAppName(String appId) {
        AppMonitoringConfig config = configService.getById(appId);
        return config != null ? config.getAppName() : appId;
    }
}
