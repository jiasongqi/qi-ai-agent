package com.yupi.yuaiagent.competitor.agent;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 竞品分析智能体工厂
 * <p>
 * 负责加载 Prompt 模板，每次调用 create() 生成新的 Agent 实例，
 * 解决原 @Component 单例模式下的并发安全问题。
 */
@Slf4j
@Component
public class CompetitorAnalysisAgentFactory {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Value("classpath:prompts/competitor-analysis-prompt.txt")
    private Resource promptResource;

    private String systemPromptTemplate;

    @PostConstruct
    public void initPrompt() {
        try {
            if (promptResource != null && promptResource.exists()) {
                this.systemPromptTemplate = FileCopyUtils.copyToString(
                        new InputStreamReader(promptResource.getInputStream(), StandardCharsets.UTF_8));
                log.info("成功加载竞品分析 Prompt，来源: {}", promptResource.getURI());
            } else {
                this.systemPromptTemplate = loadDefaultPrompt();
                log.warn("外部 Prompt 文件未找到，使用默认内置 Prompt");
            }
        } catch (Exception e) {
            this.systemPromptTemplate = loadDefaultPrompt();
            log.error("加载外部 Prompt 失败，使用默认内置 Prompt: {}", e.getMessage());
        }
    }

    /**
     * 创建新的 CompetitorAnalysisAgent 实例（并发安全）
     */
    public CompetitorAnalysisAgent create() {
        ChatClient chatClient = chatClientBuilder.build();
        return new CompetitorAnalysisAgent(chatClient, systemPromptTemplate);
    }

    /**
     * 获取当前加载的 Prompt 模板（可用于调试/展示）
     */
    public String getPromptTemplate() {
        return systemPromptTemplate;
    }

    private String loadDefaultPrompt() {
        return """
            你是印尼金融科技市场情报分析专家，专注于分析印尼借贷类App的市场动态。
            分析维度：准入门槛、产品策略、下款速度、用户口碑、风险信号。
            输出 Markdown 格式，标注置信度和预警级别。
            """;
    }
}
