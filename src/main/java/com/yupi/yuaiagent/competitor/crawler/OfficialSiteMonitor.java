package com.yupi.yuaiagent.competitor.crawler;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.yupi.yuaiagent.competitor.entity.CompetitorPolicyChange;
import com.yupi.yuaiagent.competitor.mapper.CompetitorPolicyChangeMapper;
import com.yupi.yuaiagent.tools.WebScrapingTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 官网动态监控服务
 * 利用 WebScrapingTool 监控竞品官网条款变化
 * 使用 java-diff-utils 进行精确的文本差异检测
 */
@Slf4j
@Component
public class OfficialSiteMonitor {

    @Autowired
    private WebScrapingTool webScrapingTool;

    @Autowired
    private CompetitorPolicyChangeMapper policyChangeMapper;

    /**
     * 监控指定官网页面的条款变化
     *
     * @param appId     App ID
     * @param policyUrl 官网条款页面 URL
     * @param policyType 条款类型：terms, privacy, lending_policy 等
     * @return 检测到的变化，如无变化返回 null
     */
    public CompetitorPolicyChange monitorPolicyChange(String appId, String policyUrl, String policyType) {
        try {
            log.info("监控官网条款变化，appId={}, policyType={}, url={}", appId, policyType, policyUrl);

            // 1. 抓取当前页面内容
            String currentContent = webScrapingTool.scrapeWebPage(policyUrl);
            if (currentContent == null || currentContent.startsWith("Error")) {
                log.warn("抓取页面失败，appId={}, url={}", appId, policyUrl);
                return null;
            }

            // 2. 获取上次抓取的历史快照
            CompetitorPolicyChange lastSnapshot = getLatestSnapshot(appId, policyType);

            // 3. 如果是首次监控，仅保存快照
            if (lastSnapshot == null) {
                CompetitorPolicyChange snapshot = new CompetitorPolicyChange();
                snapshot.setAppId(appId);
                snapshot.setPolicyType(policyType);
                snapshot.setPolicyUrl(policyUrl);
                snapshot.setContentSnapshot(currentContent);
                policyChangeMapper.insert(snapshot);
                log.info("首次保存 [{}] 的 {} 页面快照", appId, policyType);
                return null;
            }

            // 4. 对比内容差异
            String lastContent = lastSnapshot.getContentSnapshot();
            if (lastContent != null && lastContent.equals(currentContent)) {
                log.info("[{}] 的 {} 页面无变化", appId, policyType);
                return null;
            }

            // 5. 使用 java-diff-utils 生成精确差异
            String diffContent = generateDiff(lastContent, currentContent);

            CompetitorPolicyChange change = new CompetitorPolicyChange();
            change.setAppId(appId);
            change.setPolicyType(policyType);
            change.setPolicyUrl(policyUrl);
            change.setContentSnapshot(currentContent);
            change.setDiffContent(diffContent);
            policyChangeMapper.insert(change);

            log.info("检测到 [{}] 的 {} 页面发生变化", appId, policyType);
            return change;

        } catch (Exception e) {
            log.error("监控官网条款变化失败，appId={}: {}", appId, e.getMessage());
            return null;
        }
    }

    private CompetitorPolicyChange getLatestSnapshot(String appId, String policyType) {
        try {
            return policyChangeMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CompetitorPolicyChange>()
                            .eq(CompetitorPolicyChange::getAppId, appId)
                            .eq(CompetitorPolicyChange::getPolicyType, policyType)
                            .orderByDesc(CompetitorPolicyChange::getDetectedAt)
                            .last("LIMIT 1")
            ).stream().findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 使用 java-diff-utils 进行精确的行级差异检测
     * 修复原手写对齐 diff 的噪音 Bug
     */
    private String generateDiff(String oldContent, String newContent) {
        if (oldContent == null || newContent == null) {
            return "首次对比或内容缺失";
        }
        try {
            List<String> oldLines = Arrays.asList(oldContent.split("\\n"));
            List<String> newLines = Arrays.asList(newContent.split("\\n"));

            Patch<String> patch = DiffUtils.diff(oldLines, newLines);

            if (patch.getDeltas().isEmpty()) {
                return "内容整体结构相同，可能存在细微格式差异";
            }

            StringBuilder diff = new StringBuilder();
            for (AbstractDelta<String> delta : patch.getDeltas()) {
                DeltaType type = delta.getType();
                int sourcePos = delta.getSource().getPosition();
                int targetPos = delta.getTarget().getPosition();

                diff.append(String.format("@@ 行 %d -> %d [%s] @@\n", sourcePos, targetPos, type));

                switch (type) {
                    case DELETE ->
                        delta.getSource().getLines().forEach(line ->
                            diff.append("- ").append(line.trim()).append("\n"));
                    case INSERT ->
                        delta.getTarget().getLines().forEach(line ->
                            diff.append("+ ").append(line.trim()).append("\n"));
                    case CHANGE -> {
                        delta.getSource().getLines().forEach(line ->
                            diff.append("- ").append(line.trim()).append("\n"));
                        delta.getTarget().getLines().forEach(line ->
                            diff.append("+ ").append(line.trim()).append("\n"));
                    }
                    default -> diff.append("= (无变化)\n");
                }
            }
            return diff.toString();
        } catch (Exception e) {
            return "差异生成失败: " + e.getMessage();
        }
    }
}
