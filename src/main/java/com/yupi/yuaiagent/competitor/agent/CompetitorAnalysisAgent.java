package com.yupi.yuaiagent.competitor.agent;

import com.yupi.yuaiagent.agent.BaseAgent;
import com.yupi.yuaiagent.agent.model.AgentState;
import com.yupi.yuaiagent.competitor.entity.AppMonitoringConfig;
import com.yupi.yuaiagent.competitor.entity.CompetitorReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 竞品分析智能体（非 Spring 管理原型，每次分析创建新实例，并发安全）
 * <p>
 * 不要加 @Component！通过 CompetitorAnalysisAgentFactory 创建实例。
 * 系统 Prompt 从外部文件加载，支持版本化管理。
 */
@Slf4j
public class CompetitorAnalysisAgent extends BaseAgent {

    private final String systemPromptTemplate;

    /**
     * @param chatClient          ChatClient 实例
     * @param systemPromptTemplate 从外部文件加载的系统 Prompt
     */
    public CompetitorAnalysisAgent(ChatClient chatClient, String systemPromptTemplate) {
        setName("CompetitorAnalysisAgent");
        this.systemPromptTemplate = systemPromptTemplate;
        setSystemPrompt(systemPromptTemplate);
        setChatClient(chatClient);
        setMaxSteps(5);
    }

    /**
     * 执行竞品分析（单次调用，无实例状态，并发安全）
     *
     * @param reviews       待分析的评论列表
     * @param appNameMap    appId -> appName 映射
     * @param context       补充上下文（更新日志/条款变化等）
     * @return LLM 分析结果
     */
    public String analyze(List<CompetitorReview> reviews, Map<String, String> appNameMap, String context) {
        try {
            String userPrompt = buildAnalysisPrompt(reviews, appNameMap, context);

            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPromptTemplate));
            messages.add(new UserMessage(userPrompt));

            Prompt prompt = new Prompt(messages);
            String response = getChatClient().prompt(prompt).call().content();

            setState(AgentState.FINISHED);
            return response;
        } catch (Exception e) {
            log.error("竞品分析智能体执行失败: {}", e.getMessage());
            setState(AgentState.ERROR);
            return "分析失败: " + e.getMessage();
        }
    }

    @Override
    public String step() {
        // 保留 BaseAgent 契约，但实际逻辑已迁移到 analyze()
        return "请使用 analyze() 方法代替 step()";
    }

    private String buildAnalysisPrompt(List<CompetitorReview> reviews, Map<String, String> appNameMap, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 竞品数据采集结果\n\n");

        sb.append("## 数据概览\n");
        sb.append("- 评论总数: ").append(reviews.size()).append("\n");

        if (!reviews.isEmpty()) {
            var grouped = reviews.stream()
                    .collect(Collectors.groupingBy(CompetitorReview::getAppId));

            sb.append("\n## 各App评论详情\n");
            for (var entry : grouped.entrySet()) {
                String appId = entry.getKey();
                String appName = appNameMap.getOrDefault(appId, appId);
                List<CompetitorReview> appReviews = entry.getValue();
                double avgRating = appReviews.stream()
                        .filter(r -> r.getRating() != null)
                        .mapToInt(CompetitorReview::getRating)
                        .average().orElse(0);

                sb.append("\n### ").append(appName).append(" (ID: ").append(appId).append(")\n");
                sb.append("- 评论数: ").append(appReviews.size()).append("\n");
                sb.append("- 平均评分: ").append(String.format("%.1f", avgRating)).append("\n");
                sb.append("- 最新评论:\n");

                appReviews.stream()
                        .sorted((a, b) -> {
                            if (a.getReviewDate() == null || b.getReviewDate() == null) return 0;
                            return b.getReviewDate().compareTo(a.getReviewDate());
                        })
                        .limit(10)
                        .forEach(r -> {
                            sb.append("  - [").append(r.getRating()).append("星] ")
                              .append(r.getReviewTitle() != null ? r.getReviewTitle() : "无标题")
                              .append(": ")
                              .append(r.getReviewContent() != null
                                      ? r.getReviewContent().substring(0, Math.min(200, r.getReviewContent().length())) : "")
                              .append("...\n");
                        });
            }
        }

        if (context != null && !context.isEmpty()) {
            sb.append("\n## 补充上下文\n").append(context).append("\n");
        }

        sb.append("\n# 分析要求\n");
        sb.append("请基于以上数据，输出完整的竞品分析报告，包括：\n");
        sb.append("1. 核心变化摘要（本周/本期最重要的3-5个发现）\n");
        sb.append("2. 各维度详细分析（准入门槛、产品策略、下款速度、用户口碑、风险信号）\n");
        sb.append("3. 针对 Adapundi 的 actionable 建议\n");
        sb.append("4. 针对 CrediNex 的 actionable 建议\n");
        sb.append("5. 预警清单（如有）\n");

        return sb.toString();
    }
}
