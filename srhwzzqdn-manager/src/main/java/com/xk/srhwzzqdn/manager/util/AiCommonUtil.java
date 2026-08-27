package com.xk.srhwzzqdn.manager.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 通用 AI 调用工具类
 * <p>
 * 支持任意兼容 OpenAI 或阿里云百炼格式的 AI 服务，只需在 application.yml 中配置：
 * <pre>
 * ai:
 *   common:
 *     enabled: true
 *     api-type: openai          # openai（兼容DeepSeek/OpenAI/Moonshot/Zhipu等） | bailian（阿里云百炼）
 *     api-key: sk-xxx
 *     endpoint: https://api.deepseek.com/v1/chat/completions
 *     model: deepseek-chat
 *     temperature: 0.7
 *     max-tokens: 4096
 *     connect-timeout: 30000
 *     read-timeout: 60000
 * </pre>
 * <p>
 * 使用方法：
 * <pre>
 * &#64;Autowired
 * private AiCommonUtil aiCommonUtil;
 * <p>
 * // 基本调用
 * String result = aiCommonUtil.call("你的提示词");
 * <p>
 * // 带系统提示
 * String result = aiCommonUtil.callWithSystem("你是交易复盘助手", "分析今天的数据");
 * <p>
 * // 多轮对话
 * List&lt;JSONObject&gt; messages = new ArrayList&lt;&gt;();
 * messages.add(JSON.parseObject("{\"role\":\"system\",\"content\":\"你是助手\"}"));
 * messages.add(JSON.parseObject("{\"role\":\"user\",\"content\":\"你好\"}"));
 * String result = aiCommonUtil.callWithMessages(messages);
 * </pre>
 */
@Component
public class AiCommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(AiCommonUtil.class);

    @Value("${ai.common.enabled:true}")
    private boolean enabled;

    @Value("${ai.common.api-type:openai}")
    private String apiType;

    @Value("${ai.common.api-key:}")
    private String apiKey;

    @Value("${ai.common.endpoint:https://api.deepseek.com/v1/chat/completions}")
    private String endpoint;

    @Value("${ai.common.model:deepseek-chat}")
    private String model;

    @Value("${ai.common.temperature:0.7}")
    private double temperature;

    @Value("${ai.common.max-tokens:4096}")
    private int maxTokens;

    @Value("${ai.common.connect-timeout:30000}")
    private int connectTimeout;

    @Value("${ai.common.read-timeout:60000}")
    private int readTimeout;

    /**
     * 基本调用
     */
    public String call(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            logger.warn("AI调用失败：提示词为空");
            return "";
        }
        if (!checkConfig()) return "";
        return doRequest(buildMessages(null, prompt));
    }

    /**
     * 带系统提示的调用
     */
    public String callWithSystem(String systemPrompt, String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            logger.warn("AI调用失败：用户提示词为空");
            return "";
        }
        if (!checkConfig()) return "";
        return doRequest(buildMessages(systemPrompt, userPrompt));
    }

    /**
     * 多轮对话调用
     */
    public String callWithMessages(List<JSONObject> messages) {
        if (messages == null || messages.isEmpty()) {
            logger.warn("AI调用失败：消息列表为空");
            return "";
        }
        if (!checkConfig()) return "";
        return doRequest(messages);
    }

    /**
     * 检查配置
     */
    private boolean checkConfig() {
        if (!enabled) {
            logger.warn("AI调用跳过：ai.common.enabled=false");
            return false;
        }
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("AI调用失败：api-key未配置，请在application.yml中配置ai.common.api-key");
            return false;
        }
        return true;
    }

    /**
     * 构建消息列表
     */
    private List<JSONObject> buildMessages(String systemPrompt, String userPrompt) {
        JSONArray messages = new JSONArray();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            JSONObject sys = new JSONObject();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
        }
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", userPrompt);
        messages.add(user);
        return JSON.parseArray(messages.toJSONString(), JSONObject.class);
    }

    /**
     * 执行HTTP请求
     */
    private String doRequest(List<JSONObject> messages) {
        String type = apiType == null ? "openai" : apiType.toLowerCase().trim();
        JSONObject requestBody = "bailian".equals(type) ? buildBailianBody(messages) : buildOpenAiBody(messages);
        String jsonBody = requestBody.toJSONString();

        logger.info("调用AI服务 | type={} | model={} | endpoint={} | 请求体大小={} bytes",
                type, model, endpoint, jsonBody.getBytes(StandardCharsets.UTF_8).length);
        long startTime = System.currentTimeMillis();

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            long costTime = System.currentTimeMillis() - startTime;

            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                String result = parseResponse(response.toString(), type);
                logger.info("AI调用成功 | 耗时={} ms | 响应长度={} 字符", costTime, result.length());
                return result;
            } else {
                String errorMsg = readErrorStream(conn);
                logger.error("AI调用失败 | HTTP {} | 耗时={} ms | 错误: {}", responseCode, costTime, errorMsg);
                return "";
            }
        } catch (Exception e) {
            logger.error("AI调用异常 | endpoint={}", endpoint, e);
            return "";
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception e) {}
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * 构建OpenAI兼容格式请求体（DeepSeek/OpenAI/Moonshot/Zhipu等）
     */
    private JSONObject buildOpenAiBody(List<JSONObject> messages) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        body.put("stream", false);
        body.put("messages", messages);
        return body;
    }

    /**
     * 构建阿里云百炼格式请求体
     */
    private JSONObject buildBailianBody(List<JSONObject> messages) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        JSONObject parameters = new JSONObject();
        parameters.put("temperature", temperature);
        parameters.put("max_tokens", maxTokens);
        parameters.put("result_format", "message");
        body.put("parameters", parameters);
        JSONObject input = new JSONObject();
        input.put("messages", messages);
        body.put("input", input);
        return body;
    }

    /**
     * 解析响应
     */
    private String parseResponse(String responseBody, String type) {
        try {
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("AI响应为空");
                return "";
            }
            if (responseBody.trim().startsWith("<") || responseBody.trim().startsWith("<!")) {
                logger.error("AI响应不是JSON格式（可能是endpoint配置错误，返回了HTML页面）| 响应前200字符: {}",
                        responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);
                return "";
            }
            JSONObject respJson = JSON.parseObject(responseBody);
            JSONArray choices;
            if ("bailian".equals(type)) {
                JSONObject output = respJson.getJSONObject("output");
                if (output == null) { logger.warn("百炼响应未找到output字段"); return ""; }
                choices = output.getJSONArray("choices");
                printUsage(output.getJSONObject("usage"), "input_tokens", "output_tokens", "total_tokens");
            } else {
                choices = respJson.getJSONArray("choices");
                printUsage(respJson.getJSONObject("usage"), "prompt_tokens", "completion_tokens", "total_tokens");
            }
            if (choices == null || choices.isEmpty()) { logger.warn("响应未找到choices字段"); return ""; }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            if (message == null) { logger.warn("响应未找到message字段"); return ""; }
            String content = message.getString("content");
            return content != null ? content : "";
        } catch (Exception e) {
            logger.error("解析AI响应失败 | 原始响应前200字符: {}",
                    responseBody != null && responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody, e);
            return "";
        }
    }


    /**
     * 打印token消耗
     */
    private void printUsage(JSONObject usage, String inputKey, String outputKey, String totalKey) {
        if (usage == null) return;
        Integer input = usage.getInteger(inputKey);
        Integer output = usage.getInteger(outputKey);
        Integer total = usage.getInteger(totalKey);
        if (input != null && output != null && total != null) {
            logger.info("Token消耗 | 输入: {} | 输出: {} | 总计: {}", input, output, total);
        }
    }

    /**
     * 读取错误流
     */
    private String readErrorStream(HttpURLConnection conn) {
        if (conn.getErrorStream() == null) return "";
        try (BufferedReader er = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = er.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
}