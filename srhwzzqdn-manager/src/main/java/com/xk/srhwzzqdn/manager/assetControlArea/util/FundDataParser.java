package com.xk.srhwzzqdn.manager.assetControlArea.util;
import lombok.Data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 东方财富基金数据解析工具类
 * 用于解析 https://fund.eastmoney.com/pingzhongdata/{基金代码}.js 接口返回的数据
 */
public class FundDataParser {

    // ==================== 1. 基础信息解析 ====================

    /**
     * 提取字符串值（如基金名称、代码等）
     */
    public static String extractString(String content, String varName) {
        Pattern p = Pattern.compile(varName + "\\s*=\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(content);
        return m.find() ? m.group(1) : null;
    }

    /**
     * 提取数值（如收益率、费率等）
     */
    public static Double extractDouble(String content, String varName) {
        Pattern p = Pattern.compile(varName + "\\s*=\\s*\"?([0-9.-]+)\"?");
        Matcher m = p.matcher(content);
        return m.find() ? Double.parseDouble(m.group(1)) : null;
    }

    // ==================== 2. 数组数据解析 ====================

    /**
     * 提取简单字符串数组（如股票代码列表）
     */
    public static List<String> extractStringArray(String content, String varName) {
        List<String> result = new ArrayList<>();
        Pattern p = Pattern.compile(varName + "\\s*=\\s*\\[([^\\]]+)\\]");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String[] items = m.group(1).split(",");
            for (String item : items) {
                String code = item.trim().replaceAll("\"", "");
                if (!code.isEmpty()) {
                    result.add(code);
                }
            }
        }
        return result;
    }

    // ==================== 3. 净值走势数据解析（核心） ====================

    /**
     * 净值走势数据类
     */
    public static class NavData {
        private Date date;          // 日期
        private BigDecimal nav;     // 单位净值
        private BigDecimal dailyReturn; // 日收益率(%)
        private String dividendInfo;    // 分红/拆分信息
        private BigDecimal accumulatedNav; // 累计净值（可选）

        // getters and setters
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        public BigDecimal getNav() { return nav; }
        public void setNav(BigDecimal nav) { this.nav = nav; }
        public BigDecimal getDailyReturn() { return dailyReturn; }
        public void setDailyReturn(BigDecimal dailyReturn) { this.dailyReturn = dailyReturn; }
        public String getDividendInfo() { return dividendInfo; }
        public void setDividendInfo(String dividendInfo) { this.dividendInfo = dividendInfo; }
        public BigDecimal getAccumulatedNav() { return accumulatedNav; }
        public void setAccumulatedNav(BigDecimal accumulatedNav) { this.accumulatedNav = accumulatedNav; }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return String.format("NavData{date=%s, nav=%s, dailyReturn=%s, dividendInfo=%s}",
                    sdf.format(date), nav, dailyReturn, dividendInfo);
        }
    }

    /**
     * 解析单位净值走势数据（Data_netWorthTrend）
     * 返回按日期升序排列的列表
     */
    public static List<NavData> parseNetWorthTrend(String content) {
        List<NavData> list = new ArrayList<>();
        Pattern p = Pattern.compile("Data_netWorthTrend\\s*=\\s*\\[([\\s\\S]*?)\\];");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String arrayStr = getInnerArray(m.group(1));
            String[] objects = arrayStr.split("\\},\\{");
            for (String obj : objects) {
                NavData navData = parseNavObject(obj);
                if (navData != null) {
                    list.add(navData);
                }
            }
        }
        // 按时间升序排列（最早的在前）
        list.sort(Comparator.comparing(NavData::getDate));
        return list;
    }

    /**
     * 解析累计净值走势数据（Data_ACWorthTrend）
     */
    public static List<NavData> parseACWorthTrend(String content) {
        List<NavData> list = new ArrayList<>();
        Pattern p = Pattern.compile("Data_ACWorthTrend\\s*=\\s*\\[([\\s\\S]*?)\\];");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String arrayStr = getInnerArray(m.group(1));
            String[] objects = arrayStr.split("\\},\\{");
            for (String obj : objects) {
                Map<String, String> kv = parseKeyValue(obj);
                if (kv.containsKey("x") && kv.containsKey("y")) {
                    NavData navData = new NavData();
                    navData.setDate(new Date(Long.parseLong(kv.get("x"))));
                    navData.setAccumulatedNav(new BigDecimal(kv.get("y")));
                    list.add(navData);
                }
            }
        }
        list.sort(Comparator.comparing(NavData::getDate));
        return list;
    }

    /**
     * 解析单个净值对象
     */
    private static NavData parseNavObject(String obj) {
        // 处理对象开头和结尾的大括号
        obj = obj.replace("{", "").replace("}", "");
        Map<String, String> kv = parseKeyValue(obj);

        if (!kv.containsKey("x") || !kv.containsKey("y")) {
            return null;
        }

        NavData navData = new NavData();
        try {
            navData.setDate(new Date(Long.parseLong(kv.get("x"))));
            navData.setNav(new BigDecimal(kv.get("y")));

            if (kv.containsKey("equityReturn") && !kv.get("equityReturn").isEmpty()) {
                navData.setDailyReturn(new BigDecimal(kv.get("equityReturn")));
            }
            if (kv.containsKey("unitMoney") && !kv.get("unitMoney").isEmpty()) {
                navData.setDividendInfo(kv.get("unitMoney"));
            }
        } catch (Exception e) {
            return null;
        }
        return navData;
    }

    /**
     * 解析对象中的键值对（处理引号）
     */
    private static Map<String, String> parseKeyValue(String objStr) {
        Map<String, String> result = new HashMap<>();
        // 按逗号分割，但要小心值内部可能包含逗号
        String[] pairs = objStr.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] parts = pair.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim().replaceAll("\"", "");
                String value = parts[1].trim().replaceAll("^\"|\"$", "");
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 获取数组内部内容（考虑嵌套）
     */
    private static String getInnerArray(String arrayContent) {
        int start = arrayContent.indexOf('[');
        int end = arrayContent.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return arrayContent.substring(start + 1, end);
        }
        return arrayContent;
    }

    // ==================== 4. 股票仓位数据解析 ====================

    public static class PositionData {
        private Date date;
        private BigDecimal positionPercent; // 股票仓位百分比

        public PositionData(long timestamp, double percent) {
            this.date = new Date(timestamp);
            this.positionPercent = new BigDecimal(percent);
        }
        public Date getDate() { return date; }
        public BigDecimal getPositionPercent() { return positionPercent; }
    }

    public static List<PositionData> parseSharesPositions(String content) {
        List<PositionData> list = new ArrayList<>();
        Pattern p = Pattern.compile("Data_fundSharesPositions\\s*=\\s*\\[([\\s\\S]*?)\\];");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String arrayStr = m.group(1);
            Pattern entry = Pattern.compile("\\[([0-9]+),([0-9.]+)\\]");
            Matcher em = entry.matcher(arrayStr);
            while (em.find()) {
                list.add(new PositionData(Long.parseLong(em.group(1)), Double.parseDouble(em.group(2))));
            }
        }
        return list;
    }

    // ==================== 5. 累计收益率走势解析 ====================

    public static class GrandTotalData {
        private String name;
        private List<NavData> data;
        // getters and setters...
    }

    public static List<GrandTotalData> parseGrandTotal(String content) {
        List<GrandTotalData> result = new ArrayList<>();
        // 解析Data_grandTotal数组
        Pattern p = Pattern.compile("Data_grandTotal\\s*=\\s*\\[([\\s\\S]*?)\\];");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String arrayStr = getInnerArray(m.group(1));
            // 按"}]},{"分割每个基金对象
            String[] objects = arrayStr.split("\\},\\{");
            for (String obj : objects) {
                GrandTotalData gd = new GrandTotalData();
                // 提取name
                Pattern nameP = Pattern.compile("\"name\":\"([^\"]+)\"");
                Matcher nameM = nameP.matcher(obj);
                if (nameM.find()) {
                    gd.name = nameM.group(1);
                }
                // 提取data数组
                Pattern dataP = Pattern.compile("\"data\":\\[([\\s\\S]*)\\]");
                Matcher dataM = dataP.matcher(obj);
                if (dataM.find()) {
                    String dataStr = dataM.group(1);
                    String[] points = dataStr.split("\\],\\[");
                    List<NavData> dataList = new ArrayList<>();
                    for (String point : points) {
                        String[] parts = point.replace("[", "").replace("]", "").split(",");
                        if (parts.length >= 2) {
                            NavData nd = new NavData();
                            nd.setDate(new Date(Long.parseLong(parts[0])));
                            nd.setAccumulatedNav(new BigDecimal(parts[1]));
                            dataList.add(nd);
                        }
                    }
                    gd.data = dataList;
                }
                result.add(gd);
            }
        }
        return result;
    }

    // ==================== 6. 同类型基金涨幅榜解析 ====================

    public static List<List<String>> parseSameTypeRanking(String content) {
        List<List<String>> result = new ArrayList<>();
        Pattern p = Pattern.compile("swithSameType\\s*=\\s*\\[([\\s\\S]*?)\\];");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String arrayStr = getInnerArray(m.group(1));
            // 按"],["分割每个子数组
            String[] subArrays = arrayStr.split("\\],\\[");
            for (String sub : subArrays) {
                List<String> list = new ArrayList<>();
                String[] items = sub.replace("[", "").replace("]", "").split(",");
                for (String item : items) {
                    list.add(item.trim().replaceAll("'", ""));
                }
                result.add(list);
            }
        }
        return result;
    }

    // ==================== 7. 资产配置解析 ====================
    @Data
    public static class AssetAllocation {
        private List<String> categories;
        private Map<String, List<Double>> series;
        private List<Double> netAssets; // 净资产

        public AssetAllocation() {
            this.series = new HashMap<>();
            this.netAssets = new ArrayList<>();
        }
        // getters and setters...
    }

    public static AssetAllocation parseAssetAllocation(String content) {
        AssetAllocation aa = new AssetAllocation();
        Pattern p = Pattern.compile("Data_assetAllocation\\s*=\\s*(\\{[\\s\\S]*?\\});");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String jsonStr = m.group(1);
            // 简化解析 - 提取categories
            Pattern catP = Pattern.compile("\"categories\":\\[([^\\]]+)\\]");
            Matcher catM = catP.matcher(jsonStr);
            if (catM.find()) {
                String[] cats = catM.group(1).split(",");
                aa.categories = new ArrayList<>();
                for (String cat : cats) {
                    aa.categories.add(cat.trim().replaceAll("\"", ""));
                }
            }
            // 解析净资产（最后一组数据）
            Pattern netP = Pattern.compile("\"data\":\\[([0-9.]+),([0-9.]+),([0-9.]+),([0-9.]+)\\]");
            Matcher netM = netP.matcher(jsonStr);
            while (netM.find()) {
                List<Double> values = new ArrayList<>();
                for (int i = 1; i <= netM.groupCount(); i++) {
                    values.add(Double.parseDouble(netM.group(i)));
                }
                aa.netAssets = values;
            }
        }
        return aa;
    }

    // ==================== 8. 基金经理信息解析 ====================
    @Data
    public static class FundManager {
        private String name;
        private int star;
        private String workTime;
        private String fundSize;
        // experience/return/risk/stability/timing
        private Map<String, Double> abilities;

        public FundManager() {
            this.abilities = new HashMap<>();
        }
        // getters and setters...
    }

    public static List<FundManager> parseCurrentFundManager(String content) {
        List<FundManager> managers = new ArrayList<>();
        Pattern p = Pattern.compile("Data_currentFundManager\\s*=\\s*\\[([\\s\\S]*?)\\];");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String arrayStr = m.group(1);
            String[] managerObjs = arrayStr.split("\\},\\{");
            for (String obj : managerObjs) {
                FundManager fm = new FundManager();
                // 提取姓名
                Pattern nameP = Pattern.compile("\"name\":\"([^\"]+)\"");
                Matcher nameM = nameP.matcher(obj);
                if (nameM.find()) {
                    fm.setName(nameM.group(1));
                }
                // 提取星级
                Pattern starP = Pattern.compile("\"star\":([0-9]+)");
                Matcher starM = starP.matcher(obj);
                if (starM.find()) {
                    fm.setStar(Integer.parseInt(starM.group(1)));
                }
                // 提取从业时间
                Pattern workP = Pattern.compile("\"workTime\":\"([^\"]+)\"");
                Matcher workM = workP.matcher(obj);
                if (workM.find()) {
                    fm.setWorkTime(workM.group(1));
                }
                managers.add(fm);
            }
        }
        return managers;
    }

    // ==================== 9. 业绩评价解析 ====================

    public static Map<String, Object> parsePerformanceEvaluation(String content) {
        Map<String, Object> result = new HashMap<>();
        Pattern p = Pattern.compile("Data_performanceEvaluation\\s*=\\s*(\\{[\\s\\S]*?\\});");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String jsonStr = m.group(1);
            // 提取平均分
            Pattern avgP = Pattern.compile("\"avr\":\"([^\"]+)\"");
            Matcher avgM = avgP.matcher(jsonStr);
            if (avgM.find()) {
                result.put("avgScore", Double.parseDouble(avgM.group(1)));
            }
            // 提取各项得分
            Pattern dataP = Pattern.compile("\"data\":\\[([^\\]]+)\\]");
            Matcher dataM = dataP.matcher(jsonStr);
            if (dataM.find()) {
                String[] scores = dataM.group(1).split(",");
                List<Double> scoreList = new ArrayList<>();
                for (String score : scores) {
                    scoreList.add(Double.parseDouble(score.trim()));
                }
                result.put("scores", scoreList);
            }
            // 提取分类名称
            Pattern catP = Pattern.compile("\"categories\":\\[([^\\]]+)\\]");
            Matcher catM = catP.matcher(jsonStr);
            if (catM.find()) {
                String[] cats = catM.group(1).split(",");
                List<String> catList = new ArrayList<>();
                for (String cat : cats) {
                    catList.add(cat.trim().replaceAll("\"", ""));
                }
                result.put("categories", catList);
            }
        }
        return result;
    }

    // ==================== 10. 统一数据集合 ====================

    public static class FundData {
        // 基础信息
        private String fundName;
        private String fundCode;
        private Double sourceRate;
        private Double currentRate;
        private List<String> stockCodes;
        private List<String> stockCodesNew;

        // 收益率
        private Double return1Year;
        private Double return6Month;
        private Double return3Month;
        private Double return1Month;

        // 净值数据（最重要）
        private List<NavData> netWorthTrend;
        private List<NavData> accumulatedWorthTrend;

        // 其他数据
        private List<PositionData> sharesPositions;
        private List<GrandTotalData> grandTotal;
        private AssetAllocation assetAllocation;
        private List<FundManager> fundManagers;
        private Map<String, Object> performanceEvaluation;

        // getters and setters...
        public String getFundName() { return fundName; }
        public void setFundName(String fundName) { this.fundName = fundName; }
        public String getFundCode() { return fundCode; }
        public void setFundCode(String fundCode) { this.fundCode = fundCode; }
        public Double getSourceRate() { return sourceRate; }
        public void setSourceRate(Double sourceRate) { this.sourceRate = sourceRate; }
        public Double getCurrentRate() { return currentRate; }
        public void setCurrentRate(Double currentRate) { this.currentRate = currentRate; }
        public List<String> getStockCodes() { return stockCodes; }
        public void setStockCodes(List<String> stockCodes) { this.stockCodes = stockCodes; }
        public List<String> getStockCodesNew() { return stockCodesNew; }
        public void setStockCodesNew(List<String> stockCodesNew) { this.stockCodesNew = stockCodesNew; }
        public Double getReturn1Year() { return return1Year; }
        public void setReturn1Year(Double return1Year) { this.return1Year = return1Year; }
        public Double getReturn6Month() { return return6Month; }
        public void setReturn6Month(Double return6Month) { this.return6Month = return6Month; }
        public Double getReturn3Month() { return return3Month; }
        public void setReturn3Month(Double return3Month) { this.return3Month = return3Month; }
        public Double getReturn1Month() { return return1Month; }
        public void setReturn1Month(Double return1Month) { this.return1Month = return1Month; }
        public List<NavData> getNetWorthTrend() { return netWorthTrend; }
        public void setNetWorthTrend(List<NavData> netWorthTrend) { this.netWorthTrend = netWorthTrend; }
        public List<NavData> getAccumulatedWorthTrend() { return accumulatedWorthTrend; }
        public void setAccumulatedWorthTrend(List<NavData> accumulatedWorthTrend) { this.accumulatedWorthTrend = accumulatedWorthTrend; }
        public List<PositionData> getSharesPositions() { return sharesPositions; }
        public void setSharesPositions(List<PositionData> sharesPositions) { this.sharesPositions = sharesPositions; }
        public List<GrandTotalData> getGrandTotal() { return grandTotal; }
        public void setGrandTotal(List<GrandTotalData> grandTotal) { this.grandTotal = grandTotal; }
        public AssetAllocation getAssetAllocation() { return assetAllocation; }
        public void setAssetAllocation(AssetAllocation assetAllocation) { this.assetAllocation = assetAllocation; }
        public List<FundManager> getFundManagers() { return fundManagers; }
        public void setFundManagers(List<FundManager> fundManagers) { this.fundManagers = fundManagers; }
        public Map<String, Object> getPerformanceEvaluation() { return performanceEvaluation; }
        public void setPerformanceEvaluation(Map<String, Object> performanceEvaluation) { this.performanceEvaluation = performanceEvaluation; }
    }

    /**
     * 解析完整基金数据（统一入口）
     */
    public static FundData parseAllFundData(String content) {
        FundData fundData = new FundData();

        // 1. 基础信息
        fundData.setFundName(extractString(content, "fS_name"));
        fundData.setFundCode(extractString(content, "fS_code"));
        fundData.setSourceRate(extractDouble(content, "fund_sourceRate"));
        fundData.setCurrentRate(extractDouble(content, "fund_Rate"));

        // 2. 持仓股票
        fundData.setStockCodes(extractStringArray(content, "stockCodes"));
        fundData.setStockCodesNew(extractStringArray(content, "stockCodesNew"));

        // 3. 收益率
        fundData.setReturn1Year(extractDouble(content, "syl_1n"));
        fundData.setReturn6Month(extractDouble(content, "syl_6y"));
        fundData.setReturn3Month(extractDouble(content, "syl_3y"));
        fundData.setReturn1Month(extractDouble(content, "syl_1y"));

        // 4. 净值数据（核心）
        fundData.setNetWorthTrend(parseNetWorthTrend(content));
        fundData.setAccumulatedWorthTrend(parseACWorthTrend(content));

        // 5. 其他数据
        fundData.setSharesPositions(parseSharesPositions(content));
        fundData.setGrandTotal(parseGrandTotal(content));
        fundData.setAssetAllocation(parseAssetAllocation(content));
        fundData.setFundManagers(parseCurrentFundManager(content));
        fundData.setPerformanceEvaluation(parsePerformanceEvaluation(content));

        return fundData;
    }

    // ==================== 11. 测试方法 ====================

    public static void main(String[] args) {
        // 假设已获取到接口内容
        String content = fetchFundDataFromApi("460001");

        // 解析完整数据
        FundData fundData = parseAllFundData(content);

        System.out.println("========== 基金基本信息 ==========");
        System.out.println("基金名称: " + fundData.getFundName());
        System.out.println("基金代码: " + fundData.getFundCode());
        System.out.println("原费率: " + fundData.getSourceRate() + "%");
        System.out.println("现费率: " + fundData.getCurrentRate() + "%");

        System.out.println("\n========== 收益率 ==========");
        System.out.println("近1月: " + fundData.getReturn1Month() + "%");
        System.out.println("近3月: " + fundData.getReturn3Month() + "%");
        System.out.println("近6月: " + fundData.getReturn6Month() + "%");
        System.out.println("近1年: " + fundData.getReturn1Year() + "%");

        System.out.println("\n========== 持仓股票 ==========");
        if (fundData.getStockCodesNew() != null) {
            for (String code : fundData.getStockCodesNew()) {
                System.out.println(code);
            }
        }

        System.out.println("\n========== 净值数据 ==========");
        System.out.println("共 " + fundData.getNetWorthTrend().size() + " 条净值记录");
        System.out.println("最新净值: " + fundData.getNetWorthTrend().get(fundData.getNetWorthTrend().size() - 1));
        System.out.println("最早净值: " + fundData.getNetWorthTrend().get(0));

        System.out.println("\n========== 基金经理 ==========");
        if (fundData.getFundManagers() != null) {
            for (FundManager fm : fundData.getFundManagers()) {
                System.out.println("姓名: " + fm.getName() + ", 星级: " + fm.getStar() + ", 从业: " + fm.getWorkTime());
            }
        }
    }

    /**
     * 获取接口数据（示例）
     */
    private static String fetchFundDataFromApi(String fundCode) {
        // 实际使用时替换为HTTP请求代码
        // 这里返回示例数据或通过HTTP客户端请求
        return "";
    }
}