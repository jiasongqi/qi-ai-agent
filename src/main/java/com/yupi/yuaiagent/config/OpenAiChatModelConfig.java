package com.yupi.yuaiagent.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * OpenAI ChatModel 配置类
 * 用于配置公司部署的 Qwen/Qwen3-32B 模型
 */
@Configuration
public class OpenAiChatModelConfig {

    @Value("${spring.ai.openai.api-key:dummy}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:http://172.20.1.159:8001}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:Qwen/Qwen3-32B}")
    private String model;

    @Bean
    @Primary
    public OpenAiChatModel openAiChatModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(chatOptions)
                .build();
    }

    /**
     * 配置 EmbeddingModel，用于 RAG 向量存储
     * 注意：如果模型服务器不支持 embedding 接口，可能需要禁用 RAG 相关功能
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        return new OpenAiEmbeddingModel(openAiApi);
    }
}
