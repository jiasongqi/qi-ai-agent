package com.yupi.yuaiagent.competitor.crawler;

import cn.hutool.http.HttpUtil;
import com.yupi.yuaiagent.competitor.entity.CompetitorChangelog;
import com.yupi.yuaiagent.competitor.mapper.CompetitorChangelogMapper;
import com.yupi.yuaiagent.competitor.service.AppMonitoringConfigService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * App Store 应用更新日志抓取器
 */
@Slf4j
@Component
public class AppStoreUpdateLogCrawler {

    @Autowired
    private CompetitorChangelogMapper changelogMapper;

    private static final String APP_STORE_URL = "https://apps.apple.com/id/app/id%s";

    /**
     * 抓取指定 App 的最新版本更新日志
     */
    public CompetitorChangelog fetchLatestChangelog(String appId) {
        try {
            String url = String.format(APP_STORE_URL, appId);
            log.info("抓取 App Store 更新日志，URL: {}", url);

            String html = HttpUtil.get(url, 30000);
            Document document = Jsoup.parse(html);

            // 提取版本号
            String version = extractVersion(document);
            // 提取更新日期
            LocalDate releaseDate = extractReleaseDate(document);
            // 提取更新日志内容
            String changelogContent = extractChangelogContent(document);

            if (changelogContent == null || changelogContent.isEmpty()) {
                log.warn("未找到更新日志内容，appId={}", appId);
                return null;
            }

            // 检查是否已存在相同版本的记录
            CompetitorChangelog existing = findExistingChangelog(appId, version);
            if (existing != null) {
                log.info("版本 {} 的更新日志已存在，跳过保存", version);
                return existing;
            }

            CompetitorChangelog changelog = new CompetitorChangelog();
            changelog.setAppId(appId);
            changelog.setVersion(version);
            changelog.setReleaseDate(releaseDate);
            changelog.setChangelogContent(changelogContent);
            changelogMapper.insert(changelog);

            log.info("成功保存 [{}] 版本 {} 的更新日志", appId, version);
            return changelog;
        } catch (Exception e) {
            log.error("抓取更新日志失败，appId={}: {}", appId, e.getMessage());
            return null;
        }
    }

    private String extractVersion(Document document) {
        try {
            Element versionElem = document.select("p.whats-new__latest__version").first();
            if (versionElem != null) {
                String text = versionElem.text();
                if (text.contains("Version")) {
                    return text.replace("Version", "").trim();
                }
            }
            // 备选选择器
            Elements versionElems = document.select("[data-testid=version-history]");
            if (!versionElems.isEmpty()) {
                return versionElems.first().text().trim();
            }
        } catch (Exception e) {
            log.debug("提取版本号失败: {}", e.getMessage());
        }
        return "unknown";
    }

    private LocalDate extractReleaseDate(Document document) {
        try {
            Element dateElem = document.select("time").first();
            if (dateElem != null) {
                String dateStr = dateElem.attr("datetime");
                if (dateStr != null && !dateStr.isEmpty()) {
                    return LocalDate.parse(dateStr.substring(0, 10));
                }
            }
        } catch (Exception e) {
            log.debug("提取发布日期失败: {}", e.getMessage());
        }
        return LocalDate.now();
    }

    private String extractChangelogContent(Document document) {
        try {
            Element changelogElem = document.select("div.we-truncate").first();
            if (changelogElem != null) {
                return changelogElem.text().trim();
            }
            // 备选：查找包含 "What's New" 的区域
            Elements sections = document.select("section");
            for (Element section : sections) {
                Element heading = section.select("h2").first();
                if (heading != null && heading.text().toLowerCase().contains("new")) {
                    Element content = section.select("p, div").first();
                    if (content != null) {
                        return content.text().trim();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取更新日志内容失败: {}", e.getMessage());
        }
        return null;
    }

    private CompetitorChangelog findExistingChangelog(String appId, String version) {
        if (version == null || "unknown".equals(version)) {
            return null;
        }
        try {
            return changelogMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CompetitorChangelog>()
                            .eq(CompetitorChangelog::getAppId, appId)
                            .eq(CompetitorChangelog::getVersion, version)
                            .orderByDesc(CompetitorChangelog::getCreatedAt)
                            .last("LIMIT 1")
            ).stream().findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
