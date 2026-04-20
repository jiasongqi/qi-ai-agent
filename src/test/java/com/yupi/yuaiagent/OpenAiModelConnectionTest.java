package com.yupi.yuaiagent;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 OpenAI 模型连通性
 * 用于验证公司部署的 Qwen/Qwen3-32B 模型是否可以正常调用
 * 使用 RestTemplate 直接调用 API
 */
@Slf4j
public class OpenAiModelConnectionTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://172.20.1.159:8001";
    private final String model = "Qwen/Qwen3-32B";

    /**
     * 调用 OpenAI 兼容 API
     */
    private String callChatCompletion(String message) {
        String url = baseUrl + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer dummy");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        @SuppressWarnings("unchecked")
        Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");

        return (String) messageObj.get("content");
    }

    @Test
    void testModelConnection() {
        String testMessage = "你好，请回复'模型连接成功'，并告诉我你是谁";

        log.info("正在测试模型连接...");
        log.info("测试消息: {}", testMessage);

        try {
            String result = callChatCompletion(testMessage);

            log.info("模型回复: {}", result);

            assertNotNull(result, "模型回复不应为空");
            assertFalse(result.trim().isEmpty(), "模型回复不应为空字符串");

            log.info("模型连接测试通过！");

        } catch (Exception e) {
            log.error("模型连接测试失败: {}", e.getMessage(), e);
            fail("模型连接失败: " + e.getMessage());
        }
    }

    @Test
    void testModelSimpleMath() {
        String testMessage = "1 + 1 等于几？请只回答数字";

        log.info("正在测试模型推理能力...");

        try {
            String result = callChatCompletion(testMessage);

            log.info("数学问题回复: {}", result);

            assertNotNull(result);
            assertTrue(result.contains("2"), "模型应正确回答 1+1=2");

            log.info("模型推理测试通过！");

        } catch (Exception e) {
            log.error("模型推理测试失败: {}", e.getMessage(), e);
            fail("模型推理测试失败: " + e.getMessage());
        }
    }
}
