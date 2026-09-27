package com.xk.srhwzzqdn.manager.system.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.system.mapper.SysCommConfigMapper;
import com.xk.srhwzzqdn.manager.system.service.SysCommConfigService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.manager.util.InterfaceConfigUtil;
import com.xk.srhwzzqdn.model.dto.system.SysCommConfigQueryDto;
import com.xk.srhwzzqdn.model.entity.system.SysCommConfig;
import com.xk.srhwzzqdn.model.vo.system.InterfaceCompareVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.*;

@Service
public class SysCommConfigServiceImpl implements SysCommConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SysCommConfigServiceImpl.class);

    @Autowired
    private SysCommConfigMapper sysCommConfigMapper;

    @Autowired
    private AiCommonUtil aiCommonUtil;

    private static final String AI_SYSTEM_PROMPT =
            "你是东方财富/腾讯/新浪等财经数据接口的迁移助手。根据接口描述和旧URL，找到一个能获取相同内容数据的最新可用URL。\n" +
            "要求：\n" +
            "1. 新URL获取的数据内容必须与旧URL完全一致，不能改变返回的数据结构和字段\n" +
            "2. 只返回URL本身，不要返回任何其他内容、解释、markdown格式或代码块标记\n" +
            "3. 如果旧URL仍然可用，直接返回旧URL本身\n" +
            "4. 只替换域名或路径，不改变参数结构（参数名、参数顺序、参数值格式均不能变）\n" +
            "5. 返回的URL必须是可以直接HTTP GET请求的完整URL\n" +
            "6. URL中形如 {xxx} 的部分是占位符（如{stock_code}={股票代码}、{art_code}={公告编号}、{fund_code}={基金代码}），" +
            "新URL必须原样保留所有占位符，不得替换、删除、改名或改变占位符格式\n" +
            "7. 常见迁移方向：push2/push2his/push2delay/1.push2/2.push2/7.push2可互换；datacenter.eastmoney.com/securities/api/data/get(旧版)与datacenter-web.eastmoney.com/api/data/v1/get(新版)参数格式不同不能直接换域名\n" +
            "8. 如果不确定新URL是否可用，返回旧URL本身（保守策略）";

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{[^}]+\\}");
    // %s/%d 等String.format占位符
    private static final Pattern FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("%[sd]");

    @Override
    public PageInfo<SysCommConfig> findPage(Integer current, Integer limit, SysCommConfigQueryDto queryDto) {
        PageHelper.startPage(current, limit);
        List<SysCommConfig> list = sysCommConfigMapper.findList(queryDto);
        return new PageInfo<>(list);
    }

    @Override
    public List<SysCommConfig> findAllEnabled() {
        return sysCommConfigMapper.findAllEnabled();
    }

    @Override
    public SysCommConfig findById(String id) {
        return sysCommConfigMapper.findById(id);
    }

    @Override
    public void save(SysCommConfig config) {
        if (config.getStatus() == null) config.setStatus(1);
        sysCommConfigMapper.insert(config);
        InterfaceConfigUtil.refreshCache();
    }

    @Override
    public void update(SysCommConfig config) {
        sysCommConfigMapper.update(config);
        InterfaceConfigUtil.refreshCache();
    }

    @Override
    public void deleteById(String id) {
        sysCommConfigMapper.deleteById(id);
        InterfaceConfigUtil.refreshCache();
    }

    @Override
    public List<InterfaceCompareVo> aiFetchLatest() {
        List<SysCommConfig> allEnabled = sysCommConfigMapper.findAllEnabled();
        if (allEnabled == null || allEnabled.isEmpty()) return new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<InterfaceCompareVo>> futures = new ArrayList<>();
        for (SysCommConfig config : allEnabled) {
            futures.add(executor.submit(() -> fetchOne(config)));
        }
        List<InterfaceCompareVo> result = new ArrayList<>();
        for (Future<InterfaceCompareVo> f : futures) {
            try {
                InterfaceCompareVo vo = f.get(5, TimeUnit.MINUTES);
                if (vo != null) result.add(vo);
            } catch (Exception e) {
                logger.warn("AI获取接口失败: {}", e.getMessage());
            }
        }
        executor.shutdown();
        return result;
    }

    private InterfaceCompareVo fetchOne(SysCommConfig config) {
        InterfaceCompareVo vo = new InterfaceCompareVo();
        vo.setId(config.getId());
        vo.setInterfaceName(config.getInterfaceName());
        vo.setOldUrl(config.getValue());
        vo.setDescription(config.getDescription());
        vo.setCategory(config.getCategory());

        Set<String> placeholders = extractPlaceholders(config.getValue());
        String userPrompt = buildUserPrompt(config, placeholders);
        String aiResult = aiCommonUtil.callWithSystem(AI_SYSTEM_PROMPT, userPrompt);
        String newUrl = parseAiUrl(aiResult);

        if (newUrl != null && !validatePlaceholders(newUrl, placeholders)) {
            logger.warn("AI返回URL占位符校验失败,放弃新URL | id={} | 旧URL={} | 新URL={} | 应保留占位符={}",
                    config.getId(), config.getValue(), newUrl, placeholders);
            newUrl = null;
        }

        // 新URL可用性验证 + 数据结构比对
        boolean validated = false;
        String validateMsg = "未验证";
        if (newUrl != null && !newUrl.equals(config.getValue())) {
            String testUrl = fillPlaceholders(newUrl);
            String oldTestUrl = fillPlaceholders(config.getValue());
            // 1) 新URL HTTP可达性验证
            String newResp = httpGetQuiet(testUrl);
            if (newResp == null) {
                validateMsg = "新URL请求失败(HTTP不可达或超时),不建议更新";
                logger.warn("AI新URL可用性验证失败(HTTP不可达) | id={} | newUrl={}", config.getId(), testUrl);
                newUrl = null;
            } else {
                // 2) 新旧URL数据结构比对（仅当旧URL也可达时比对）
                String oldResp = httpGetQuiet(oldTestUrl);
                if (oldResp != null) {
                    boolean structMatch = compareJsonStructure(oldResp, newResp);
                    if (!structMatch) {
                        validateMsg = "新URL数据结构与旧URL不一致(顶层字段变化),请人工确认";
                        logger.warn("AI新URL数据结构比对不一致 | id={} | oldUrl={} | newUrl={}", config.getId(), oldTestUrl, testUrl);
                    } else {
                        validateMsg = "验证通过(HTTP可达+数据结构一致)";
                    }
                } else {
                    validateMsg = "新URL可达但旧URL不可达(无法比对结构),请人工确认";
                }
                validated = true;
            }
        } else if (newUrl != null && newUrl.equals(config.getValue())) {
            validateMsg = "AI返回旧URL本身(未变化)";
            validated = true;
        }

        vo.setNewUrl(newUrl != null ? newUrl : config.getValue());
        vo.setChanged(newUrl != null && !newUrl.equals(config.getValue()));
        vo.setValidated(validated);
        vo.setValidateMsg(validateMsg);
        return vo;
    }

    private String buildUserPrompt(SysCommConfig config, Set<String> placeholders) {
        StringBuilder sb = new StringBuilder();
        sb.append("接口id: ").append(config.getId()).append("\n");
        sb.append("接口名称: ").append(config.getInterfaceName()).append("\n");
        sb.append("接口描述: ").append(config.getDescription()).append("\n");
        sb.append("旧URL: ").append(config.getValue()).append("\n");
        if (!placeholders.isEmpty()) {
            sb.append("占位符: ").append(String.join(", ", placeholders)).append("\n");
            sb.append("注意: 上述占位符代表动态参数,新URL必须原样保留每一个占位符,不得替换为具体值或改名\n");
        }
        sb.append("\n请找到一个能获取相同内容数据的最新可用URL，只返回URL本身。");
        return sb.toString();
    }

    private Set<String> extractPlaceholders(String url) {
        Set<String> set = new HashSet<>();
        if (url == null) return set;
        Matcher m = PLACEHOLDER_PATTERN.matcher(url);
        while (m.find()) set.add(m.group());
        Matcher m2 = FORMAT_PLACEHOLDER_PATTERN.matcher(url);
        while (m2.find()) set.add(m2.group());
        return set;
    }

    private boolean validatePlaceholders(String newUrl, Set<String> requiredPlaceholders) {
        if (requiredPlaceholders.isEmpty()) return true;
        for (String ph : requiredPlaceholders) {
            if (!newUrl.contains(ph)) return false;
        }
        return true;
    }

    /** 用示例值替换占位符，生成可请求的测试URL */
    private String fillPlaceholders(String url) {
        if (url == null) return null;
        return url.replace("{stock_code}", "000001")
                .replace("{art_code}", "AN20240101123456")
                .replace("{fund_code}", "000001");
    }

    /** 静默HTTP GET（不打印异常堆栈，仅返回body或null） */
    private String httpGetQuiet(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setConfig(RequestConfig.custom()
                    .setConnectTimeout(8000).setSocketTimeout(10000).build());
            try (CloseableHttpResponse resp = client.execute(request)) {
                if (resp.getStatusLine().getStatusCode() == 200) {
                    String body = EntityUtils.toString(resp.getEntity(), "UTF-8");
                    if (body != null && !body.trim().isEmpty()) return body;
                }
            }
        } catch (Exception e) {
            // 静默，不打印堆栈
        }
        return null;
    }

    /** 比较两个JSON响应的顶层结构是否一致（字段名集合比对） */
    private boolean compareJsonStructure(String oldResp, String newResp) {
        try {
            com.alibaba.fastjson.JSONObject oldJson = com.alibaba.fastjson.JSON.parseObject(oldResp);
            com.alibaba.fastjson.JSONObject newJson = com.alibaba.fastjson.JSON.parseObject(newResp);
            if (oldJson == null || newJson == null) return true; // 非JSON无法比对，放行
            Set<String> oldKeys = oldJson.keySet();
            Set<String> newKeys = newJson.keySet();
            // 新URL响应应包含旧URL的所有顶层字段（允许新增字段，不允许删除字段）
            for (String key : oldKeys) {
                if (!newKeys.contains(key)) return false;
            }
            return true;
        } catch (Exception e) {
            return true; // 非JSON响应无法比对，放行
        }
    }

    private String parseAiUrl(String aiResult) {
        if (aiResult == null || aiResult.trim().isEmpty()) return null;
        String url = aiResult.trim();
        // 去除markdown代码块标记（```xxx ... ``` 或 ``` ... ```）
        if (url.startsWith("```")) {
            url = url.replaceAll("```[a-zA-Z]*", "").replaceAll("```", "").trim();
        }
        // 提取第一个http(s)://开头的URL
        int httpIdx = url.indexOf("http");
        if (httpIdx >= 0) {
            url = url.substring(httpIdx);
            int spaceIdx = url.indexOf(' ');
            if (spaceIdx > 0) url = url.substring(0, spaceIdx);
            int newlineIdx = url.indexOf('\n');
            if (newlineIdx > 0) url = url.substring(0, newlineIdx);
            int quoteIdx = url.indexOf('"');
            if (quoteIdx > 0) url = url.substring(0, quoteIdx);
            return url;
        }
        return null;
    }
}