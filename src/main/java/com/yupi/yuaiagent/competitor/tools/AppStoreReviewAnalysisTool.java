package com.yupi.yuaiagent.competitor.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yupi.yuaiagent.competitor.entity.CompetitorReview;
import com.yupi.yuaiagent.competitor.mapper.CompetitorReviewMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * App Store 评论分析工具
 * 供智能体调用，查询某App的近期评论统计数据
 */
@Component
public class AppStoreReviewAnalysisTool {

    @Autowired
    private CompetitorReviewMapper reviewMapper;

    @Tool(description = "获取指定App最近N天的评论统计摘要，包括评论数、平均评分、高频关键词")
    public String getReviewSummary(
            @ToolParam(description = "App的iOS ID，如 1435044790") String appId,
            @ToolParam(description = "查询最近多少天的评论，默认7天") int days) {

        LocalDate startDate = LocalDate.now().minusDays(days);
        LambdaQueryWrapper<CompetitorReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitorReview::getAppId, appId)
               .ge(CompetitorReview::getReviewDate, startDate)
               .orderByDesc(CompetitorReview::getReviewDate);

        List<CompetitorReview> reviews = reviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            return String.format("App ID %s 在最近 %d 天内没有新评论。", appId, days);
        }

        double avgRating = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(CompetitorReview::getRating)
                .average().orElse(0);

        Map<Integer, Long> ratingDistribution = reviews.stream()
                .filter(r -> r.getRating() != null)
                .collect(Collectors.groupingBy(CompetitorReview::getRating, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("App ID %s 最近 %d 天评论统计:\n", appId, days));
        sb.append(String.format("- 评论总数: %d\n", reviews.size()));
        sb.append(String.format("- 平均评分: %.1f\n", avgRating));
        sb.append("- 评分分布:\n");
        ratingDistribution.forEach((rating, count) ->
            sb.append(String.format("  %d星: %d条\n", rating, count))
        );

        // 提取低分评论摘要（1-2星）
        List<CompetitorReview> lowRatings = reviews.stream()
                .filter(r -> r.getRating() != null && r.getRating() <= 2)
                .limit(5)
                .collect(Collectors.toList());

        if (!lowRatings.isEmpty()) {
            sb.append("- 低分评论摘要（1-2星）:\n");
            lowRatings.forEach(r -> {
                String title = r.getReviewTitle() != null ? r.getReviewTitle() : "无标题";
                String content = r.getReviewContent() != null ? r.getReviewContent() : "";
                sb.append(String.format("  [%d星] %s: %s\n", r.getRating(), title,
                        content.substring(0, Math.min(100, content.length()))));
            });
        }

        return sb.toString();
    }

    @Tool(description = "获取指定App评分最高的N条评论")
    public String getTopReviews(
            @ToolParam(description = "App的iOS ID") String appId,
            @ToolParam(description = "获取条数，默认5条") int limit) {

        LambdaQueryWrapper<CompetitorReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitorReview::getAppId, appId)
               .orderByDesc(CompetitorReview::getRating)
               .last("LIMIT " + limit);

        List<CompetitorReview> reviews = reviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            return String.format("App ID %s 暂无评论数据。", appId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("App ID %s 的高分评论（前%d条）:\n\n", appId, reviews.size()));
        reviews.forEach(r -> {
            sb.append(String.format("评分: %d星\n", r.getRating()));
            sb.append(String.format("标题: %s\n", r.getReviewTitle()));
            sb.append(String.format("内容: %s\n", r.getReviewContent()));
            sb.append(String.format("日期: %s\n", r.getReviewDate()));
            sb.append("---\n");
        });

        return sb.toString();
    }
}
