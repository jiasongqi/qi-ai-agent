package com.yupi.yuaiagent.competitor.crawler;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yuaiagent.competitor.entity.CompetitorReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * App Store RSS 评论抓取器
 * 使用苹果官方公开 RSS Feed 接口
 */
@Slf4j
@Component
public class AppStoreReviewCrawler {

    @Value("${competitor.monitor.review.country:id}")
    private String country;

    private static final String RSS_FEED_URL = "https://itunes.apple.com/%s/rss/customerreviews/id=%s/sortBy=mostRecent/json";

    /**
     * 抓取指定 App 的评论
     *
     * @param appId    iOS App ID
     * @param maxPages 最大页数（RSS 接口一页约50条）
     * @return 评论列表
     */
    public List<CompetitorReview> fetchReviews(String appId, int maxPages) {
        List<CompetitorReview> allReviews = new ArrayList<>();
        try {
            String url = String.format(RSS_FEED_URL, country, appId);
            log.info("抓取 App Store 评论，URL: {}", url);

            String response = HttpUtil.get(url, 30000);
            if (response == null || response.isEmpty()) {
                log.warn("App Store RSS 返回空数据，appId={}", appId);
                return allReviews;
            }

            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONObject feed = jsonObject.getJSONObject("feed");
            if (feed == null) {
                log.warn("RSS 数据格式异常，缺少 feed 节点，appId={}", appId);
                return allReviews;
            }

            JSONArray entries = feed.getJSONArray("entry");
            if (entries == null || entries.isEmpty()) {
                log.info("该 App 暂无评论，appId={}", appId);
                return allReviews;
            }

            for (int i = 0; i < entries.size(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                CompetitorReview review = parseReviewEntry(entry);
                if (review != null) {
                    allReviews.add(review);
                }
            }

            log.info("成功解析 {} 条评论，appId={}", allReviews.size(), appId);
        } catch (Exception e) {
            log.error("抓取 App Store 评论失败，appId={}: {}", appId, e.getMessage());
        }
        return allReviews;
    }

    private CompetitorReview parseReviewEntry(JSONObject entry) {
        try {
            CompetitorReview review = new CompetitorReview();

            JSONObject idObj = entry.getJSONObject("id");
            if (idObj != null) {
                review.setReviewId(idObj.getStr("label"));
            }

            JSONObject author = entry.getJSONObject("author");
            if (author != null) {
                JSONObject nameObj = author.getJSONObject("name");
                if (nameObj != null) {
                    review.setReviewerName(nameObj.getStr("label"));
                }
            }

            JSONObject titleObj = entry.getJSONObject("title");
            if (titleObj != null) {
                review.setReviewTitle(titleObj.getStr("label"));
            }

            JSONObject contentObj = entry.getJSONObject("content");
            if (contentObj != null) {
                review.setReviewContent(contentObj.getStr("label"));
            }

            JSONObject ratingObj = entry.getJSONObject("im:rating");
            if (ratingObj != null) {
                review.setRating(ratingObj.getInt("label"));
            }

            JSONObject dateObj = entry.getJSONObject("updated");
            if (dateObj != null) {
                String dateStr = dateObj.getStr("label");
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        LocalDate date = LocalDate.parse(dateStr.substring(0, 10));
                        review.setReviewDate(date);
                    } catch (Exception e) {
                        log.debug("日期解析失败: {}", dateStr);
                    }
                }
            }

            return review;
        } catch (Exception e) {
            log.debug("解析单条评论失败: {}", e.getMessage());
            return null;
        }
    }
}
