package com.xk.srhwzzqdn.manager.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 阿里云百炼 AI 通用调用工具类
 * <p>
 * 使用方法：
 *   @Autowired
 *   private BailianApiUtil bailianApiUtil;
 *   <p>
 *   String result = bailianApiUtil.call("你的需求描述");
 * <p>
 * 特性：
 *   1. 自动打印 token 消耗（input_tokens, output_tokens, total_tokens）
 *   2. 支持超时时间配置
 *   3. 完善的错误处理和日志
 *   4. 支持系统提示词
 *   5. 新用户注册可领取 7000 万 Token 免费额度（有效期 90 天）
 */
@Component
public class BailianApiUtil {

    private static final Logger logger = LoggerFactory.getLogger(BailianApiUtil.class);

    @Value("${ai.bailian.api-key:}")
    private String apiKey;

    @Value("${ai.bailian.model:qwen-plus}")
    private String model;

    @Value("${ai.bailian.endpoint:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}")
    private String endpoint;

    @Value("${ai.bailian.connect-timeout:30000}")
    private int connectTimeout;

    @Value("${ai.bailian.read-timeout:60000}")
    private int readTimeout;

    /**
     * 核心方法：调用阿里云百炼 AI
     *
     * @param prompt 你的需求描述
     * @return AI 返回的字符串
     */
    public String call(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            logger.warn("调用阿里云百炼失败：需求描述为空");
            return "错误：需求描述不能为空";
        }
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("阿里云百炼 API Key 未配置，请在 application.yml 中配置 ai.bailian.api-key");
            return "错误：阿里云百炼 API Key 未配置";
        }

        logger.info("调用阿里云百炼 AI，model：{}，prompt 长度：{} 字符", model, prompt.length());
        long startTime = System.currentTimeMillis();

        String result = doRequest(prompt, null);

        long costTime = System.currentTimeMillis() - startTime;
        logger.info("阿里云百炼 AI 调用完成，耗时：{} ms", costTime);

        return result;
    }

    /**
     * 带系统提示的调用（可指定 AI 角色）
     *
     * @param systemPrompt 系统提示词（如："你是一位资深的基金研究员"）
     * @param userPrompt   用户需求描述
     * @return AI 返回的字符串
     */
    public String callWithSystem(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("阿里云百炼 API Key 未配置");
            return "错误：阿里云百炼 API Key 未配置";
        }
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            return call(userPrompt);
        }

        logger.info("调用阿里云百炼 AI（带系统提示），model：{}，system prompt 长度：{}，user prompt 长度：{}",
                model, systemPrompt.length(), userPrompt.length());
        long startTime = System.currentTimeMillis();

        String result = doRequest(userPrompt, systemPrompt);

        long costTime = System.currentTimeMillis() - startTime;
        logger.info("阿里云百炼 AI 调用完成（带系统提示），耗时：{} ms", costTime);

        return result;
    }

    /**
     * 执行请求
     */
    private String doRequest(String userPrompt, String systemPrompt) {
        JSONObject requestBody = buildRequestBody(userPrompt, systemPrompt);

        HttpsURLConnection conn = null;
        BufferedReader reader = null;

        try {
            String jsonBody = requestBody.toJSONString();
            logger.debug("请求阿里云百炼 API，请求体大小：{} bytes", jsonBody.getBytes(StandardCharsets.UTF_8).length);

            URL url = new URL(endpoint);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);

            // 发送请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 读取响应
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return parseResponse(response.toString());
            } else {
                // 读取错误信息
                String errorMsg = "";
                if (conn.getErrorStream() != null) {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorMsg = errorResponse.toString();
                }
                logger.error("阿里云百炼 API 调用失败，状态码：{}，错误信息：{}", responseCode, errorMsg);
                return "AI 接口调用失败，HTTP 状态码：" + responseCode;
            }
        } catch (Exception e) {
            logger.error("阿里云百炼 API 调用异常", e);
            return "AI 接口调用异常：" + e.getMessage();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 构建阿里云百炼请求体
     */
    private JSONObject buildRequestBody(String userPrompt, String systemPrompt) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);

        // 构建 parameters
        JSONObject parameters = new JSONObject();
        parameters.put("temperature", 0.7);
        parameters.put("max_tokens", 4096);
        parameters.put("result_format", "message");
        requestBody.put("parameters", parameters);

        // 构建 input.messages
        JSONArray messages = new JSONArray();

        // 添加系统提示（如果有）
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
        }

        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);

        JSONObject input = new JSONObject();
        input.put("messages", messages);
        requestBody.put("input", input);

        return requestBody;
    }

    /**
     * 解析阿里云百炼响应，打印 token 消耗
     */
    private String parseResponse(String responseBody) {
        try {
            JSONObject respJson = JSON.parseObject(responseBody);

            // 阿里云百炼的响应格式：output.choices[0].message.content
            JSONObject output = respJson.getJSONObject("output");
            if (output == null) {
                logger.warn("响应中未找到 output 字段");
                return "";
            }

            JSONArray choices = output.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                logger.warn("响应中未找到 choices 字段");
                return "";
            }

            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            if (message == null) {
                logger.warn("响应中未找到 message 字段");
                return "";
            }

            String content = message.getString("content");

            // 打印 token 消耗
            JSONObject usage = output.getJSONObject("usage");
            if (usage != null) {
                int inputTokens = usage.getInteger("input_tokens");
                int outputTokens = usage.getInteger("output_tokens");
                int totalTokens = usage.getInteger("total_tokens");
                logger.info("Token 消耗统计 | 输入: {} tokens | 输出: {} tokens | 总计: {} tokens",
                        inputTokens, outputTokens, totalTokens);
            } else {
                logger.warn("响应中未找到 usage 字段，无法统计 token 消耗");
            }

            return content != null ? content : "";
        } catch (Exception e) {
            logger.error("解析响应失败，原始响应：{}", responseBody, e);
            return "AI 响应解析失败：" + e.getMessage();
        }
    }
}