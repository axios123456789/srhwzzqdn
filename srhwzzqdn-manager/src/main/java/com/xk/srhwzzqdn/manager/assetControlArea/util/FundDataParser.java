package com.xk.srhwzzqdn.manager.assetControlArea.util;

import lombok.Data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 东方财富基金数据解析工具类（完整修复版）
 * 用于解析 https://fund.eastmoney.com/pingzhongdata/{基金代码}.js 接口返回的数据
 */
public class FundDataParser {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // ==================== 1. 基础信息解析 ====================

    public static String extractString(String content, String varName) {
        Pattern p = Pattern.compile(varName + "\\s*=\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(content);
        return m.find() ? m.group(1) : null;
    }

    public static Double extractDouble(String content, String varName) {
        Pattern p = Pattern.compile(varName + "\\s*=\\s*\"?([0-9.-]+)\"?");
        Matcher m = p.matcher(content);
        return m.find() ? Double.parseDouble(m.group(1)) : null;
    }

    // ==================== 2. 数组数据解析 ====================

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

    @Data
    public static class NavData {
        private Date date;
        private BigDecimal nav;              // 单位净值
        private BigDecimal dailyReturn;      // 日收益率(%)
        private String dividendInfo;         // 分红/拆分信息
        private BigDecimal accumulatedNav;   // 累计净值

        public String getDateStr() {
            return date != null ? DATE_FORMAT.format(date) : "";
        }

        @Override
        public String toString() {
            return String.format("NavData{date=%s, nav=%s, dailyReturn=%s, accNav=%s, dividend=%s}",
                    getDateStr(), nav, dailyReturn, accumulatedNav, dividendInfo);
        }
    }

    /**
     * 解析单位净值走势数据
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
                if (navData != null && navData.getDate() != null) {
                    list.add(navData);
                }
            }
        }
        list.sort(Comparator.comparing(NavData::getDate));
        return list;
    }

    /**
     * 解析累计净值走势数据（修复版：正确处理数据格式）
     */
    public static List<NavData> parseACWorthTrend(String content) {
        List<NavData> list = new ArrayList<>();
        Pattern p = Pattern.compile("Data_ACWorthTrend\\s*=\\s*\\[([\\s\\S]*?)\\];");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String arrayStr = m.group(1);
            // 累计净值数据格式为 [[timestamp, value], [timestamp, value], ...]
            Pattern entry = Pattern.compile("\\[([0-9]+),([0-9.]+)\\]");
            Matcher em = entry.matcher(arrayStr);
            while (em.find()) {
                try {
                    NavData navData = new NavData();
                    navData.setDate(new Date(Long.parseLong(em.group(1))));
                    navData.setAccumulatedNav(new BigDecimal(em.group(2)));
                    list.add(navData);
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
        }
        list.sort(Comparator.comparing(NavData::getDate));
        return list;
    }

    /**
     * 合并单位净值和累计净值
     */
    public static List<NavData> mergeNavData(List<NavData> netWorthList, List<NavData> accWorthList) {
        Map<Long, NavData> map = new HashMap<>();
        for (NavData nav : netWorthList) {
            if (nav.getDate() != null) {
                map.put(nav.getDate().getTime(), nav);
            }
        }
        for (NavData accNav : accWorthList) {
            if (accNav.getDate() != null) {
                long key = accNav.getDate().getTime();
                if (map.containsKey(key)) {
                    map.get(key).setAccumulatedNav(accNav.getAccumulatedNav());
                } else {
                    map.put(key, accNav);
                }
            }
        }
        List<NavData> result = new ArrayList<>(map.values());
        result.sort(Comparator.comparing(NavData::getDate));
        return result;
    }

    private static NavData parseNavObject(String obj) {
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

    private static Map<String, String> parseKeyValue(String objStr) {
        Map<String, String> result = new HashMap<>();
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

    private static String getInnerArray(String arrayContent) {
        int start = arrayContent.indexOf('[');
        int end = arrayContent.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return arrayContent.substring(start + 1, end);
        }
        return arrayContent;
    }

    // ==================== 4. 股票仓位数据解析 ====================

    @Data
    public static class PositionData {
        private Date date;
        private BigDecimal positionPercent;

        public PositionData(long timestamp, double percent) {
            this.date = new Date(timestamp);
            this.positionPercent = new BigDecimal(percent);
        }
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

    @Data
    public static class GrandTotalData {
        private String name;
        private List<NavData> data;
    }

    public static List<GrandTotalData> parseGrandTotal(String content) {
        List<GrandTotalData> result = new ArrayList<>();
        Pattern p = Pattern.compile("Data_grandTotal\\s*=\\s*\\[([\\s\\S]*?)\\];");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String arrayStr = getInnerArray(m.group(1));
            String[] objects = arrayStr.split("\\},\\{");
            for (String obj : objects) {
                GrandTotalData gd = new GrandTotalData();
                Pattern nameP = Pattern.compile("\"name\":\"([^\"]+)\"");
                Matcher nameM = nameP.matcher(obj);
                if (nameM.find()) {
                    gd.setName(nameM.group(1));
                }
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
                    gd.setData(dataList);
                }
                result.add(gd);
            }
        }
        return result;
    }

    // ==================== 6. 资产配置解析 ====================

    @Data
    public static class AssetAllocation {
        private List<String> categories;
        private Map<String, List<Double>> series;
        private List<Double> netAssets;
        private List<Double> stockRatio;      // 股票占净比
        private List<Double> bondRatio;       // 债券占净比
        private List<Double> cashRatio;       // 现金占净比

        public AssetAllocation() {
            this.series = new HashMap<>();
            this.netAssets = new ArrayList<>();
            this.stockRatio = new ArrayList<>();
            this.bondRatio = new ArrayList<>();
            this.cashRatio = new ArrayList<>();
        }
    }

    public static AssetAllocation parseAssetAllocation(String content) {
        AssetAllocation aa = new AssetAllocation();
        Pattern p = Pattern.compile("Data_assetAllocation\\s*=\\s*(\\{[\\s\\S]*?\\});");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String jsonStr = m.group(1);

            // 提取categories
            Pattern catP = Pattern.compile("\"categories\":\\[([^\\]]+)\\]");
            Matcher catM = catP.matcher(jsonStr);
            if (catM.find()) {
                String[] cats = catM.group(1).split(",");
                aa.setCategories(new ArrayList<>());
                for (String cat : cats) {
                    aa.getCategories().add(cat.trim().replaceAll("\"", ""));
                }
            }

            // 提取series中的股票、债券、现金数据
            Pattern seriesP = Pattern.compile("\"name\":\"([^\"]+)\".*?\"data\":\\[([0-9.,]+)\\]");
            Matcher seriesM = seriesP.matcher(jsonStr);
            while (seriesM.find()) {
                String name = seriesM.group(1);
                String dataStr = seriesM.group(2);
                String[] dataArr = dataStr.split(",");
                List<Double> dataList = new ArrayList<>();
                for (String d : dataArr) {
                    dataList.add(Double.parseDouble(d.trim()));
                }
                if ("股票占净比".equals(name)) {
                    aa.setStockRatio(dataList);
                } else if ("债券占净比".equals(name)) {
                    aa.setBondRatio(dataList);
                } else if ("现金占净比".equals(name)) {
                    aa.setCashRatio(dataList);
                }
                aa.getSeries().put(name, dataList);
            }

            // 提取净资产
            Pattern netP = Pattern.compile("\"name\":\"净资产\",\"type\":\"line\",\"data\":\\[([0-9.,]+)\\]");
            Matcher netM = netP.matcher(jsonStr);
            if (netM.find()) {
                String[] netArr = netM.group(1).split(",");
                for (String net : netArr) {
                    aa.getNetAssets().add(Double.parseDouble(net.trim()));
                }
            }
        }
        return aa;
    }

    // ==================== 7. 基金经理信息解析（修复版） ====================

    @Data
    public static class FundManager {
        private String id;
        private String name;
        private int star;
        private String workTime;
        private String fundSize;
        private String picUrl;
        private Map<String, Double> abilities;  // 能力评分

        public FundManager() {
            this.abilities = new HashMap<>();
        }
    }

    /**
     * 解析基金经理信息（修复版：精确匹配，避免误匹配其他数据）
     */
    public static List<FundManager> parseCurrentFundManager(String content) {
        List<FundManager> managers = new ArrayList<>();
        // 精确匹配基金经理对象，避免匹配到申购赎回数据
        Pattern p = Pattern.compile("\\{\"id\":\"(\\d+)\",\"pic\":\"([^\"]*)\",\"name\":\"([^\"]+)\",\"star\":(\\d+),\"workTime\":\"([^\"]*)\",\"fundSize\":\"([^\"]*)\"[^}]*\\}");
        Matcher m = p.matcher(content);
        while (m.find()) {
            FundManager fm = new FundManager();
            fm.setId(m.group(1));
            fm.setPicUrl(m.group(2));
            fm.setName(m.group(3));
            fm.setStar(Integer.parseInt(m.group(4)));
            fm.setWorkTime(m.group(5));
            fm.setFundSize(m.group(6));
            managers.add(fm);
        }
        return managers;
    }

    // ==================== 8. 业绩评价解析 ====================

    @Data
    public static class PerformanceEvaluation {
        private double avgScore;
        private List<String> categories;
        private List<Double> scores;
        private List<String> descriptions;
    }

    public static PerformanceEvaluation parsePerformanceEvaluation(String content) {
        PerformanceEvaluation pe = new PerformanceEvaluation();
        Pattern p = Pattern.compile("Data_performanceEvaluation\\s*=\\s*(\\{[\\s\\S]*?\\});");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String jsonStr = m.group(1);

            // 提取平均分
            Pattern avgP = Pattern.compile("\"avr\":\"([^\"]+)\"");
            Matcher avgM = avgP.matcher(jsonStr);
            if (avgM.find()) {
                pe.setAvgScore(Double.parseDouble(avgM.group(1)));
            }

            // 提取分类
            Pattern catP = Pattern.compile("\"categories\":\\[([^\\]]+)\\]");
            Matcher catM = catP.matcher(jsonStr);
            if (catM.find()) {
                String[] cats = catM.group(1).split(",");
                pe.setCategories(new ArrayList<>());
                for (String cat : cats) {
                    pe.getCategories().add(cat.trim().replaceAll("\"", ""));
                }
            }

            // 提取分数
            Pattern scoreP = Pattern.compile("\"data\":\\[([^\\]]+)\\]");
            Matcher scoreM = scoreP.matcher(jsonStr);
            if (scoreM.find()) {
                String[] scores = scoreM.group(1).split(",");
                pe.setScores(new ArrayList<>());
                for (String score : scores) {
                    pe.getScores().add(Double.parseDouble(score.trim()));
                }
            }

            // 提取描述
            Pattern descP = Pattern.compile("\"dsc\":\\[([^\\]]+)\\]");
            Matcher descM = descP.matcher(jsonStr);
            if (descM.find()) {
                String[] descs = descM.group(1).split(",");
                pe.setDescriptions(new ArrayList<>());
                for (String desc : descs) {
                    pe.getDescriptions().add(desc.trim().replaceAll("\"", ""));
                }
            }
        }
        return pe;
    }

    // ==================== 9. 规模变动解析 ====================

    @Data
    public static class FluctuationScale {
        private List<String> categories;
        private List<Double> scale;
        private List<String> mom;
    }

    public static FluctuationScale parseFluctuationScale(String content) {
        FluctuationScale fs = new FluctuationScale();
        Pattern p = Pattern.compile("Data_fluctuationScale\\s*=\\s*(\\{[\\s\\S]*?\\});");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String jsonStr = m.group(1);

            // 提取categories
            Pattern catP = Pattern.compile("\"categories\":\\[([^\\]]+)\\]");
            Matcher catM = catP.matcher(jsonStr);
            if (catM.find()) {
                String[] cats = catM.group(1).split(",");
                fs.setCategories(new ArrayList<>());
                for (String cat : cats) {
                    fs.getCategories().add(cat.trim().replaceAll("\"", ""));
                }
            }

            // 提取规模数据
            Pattern scaleP = Pattern.compile("\"y\":([0-9.]+),\"mom\":\"([^\"]+)\"");
            Matcher scaleM = scaleP.matcher(jsonStr);
            fs.setScale(new ArrayList<>());
            fs.setMom(new ArrayList<>());
            while (scaleM.find()) {
                fs.getScale().add(Double.parseDouble(scaleM.group(1)));
                fs.getMom().add(scaleM.group(2));
            }
        }
        return fs;
    }

    // ==================== 10. 持有人结构解析 ====================

    @Data
    public static class HolderStructure {
        private List<String> categories;
        private List<Double> institutionRatio;  // 机构持有比例
        private List<Double> individualRatio;   // 个人持有比例
        private List<Double> internalRatio;     // 内部持有比例
    }

    public static HolderStructure parseHolderStructure(String content) {
        HolderStructure hs = new HolderStructure();
        Pattern p = Pattern.compile("Data_holderStructure\\s*=\\s*(\\{[\\s\\S]*?\\});");
        Matcher m = p.matcher(content);
        if (m.find()) {
            String jsonStr = m.group(1);

            // 提取categories
            Pattern catP = Pattern.compile("\"categories\":\\[([^\\]]+)\\]");
            Matcher catM = catP.matcher(jsonStr);
            if (catM.find()) {
                String[] cats = catM.group(1).split(",");
                hs.setCategories(new ArrayList<>());
                for (String cat : cats) {
                    hs.getCategories().add(cat.trim().replaceAll("\"", ""));
                }
            }

            // 提取机构持有比例
            Pattern instP = Pattern.compile("\"name\":\"机构持有比例\",\"data\":\\[([0-9.,]+)\\]");
            Matcher instM = instP.matcher(jsonStr);
            if (instM.find()) {
                String[] data = instM.group(1).split(",");
                hs.setInstitutionRatio(new ArrayList<>());
                for (String d : data) {
                    hs.getInstitutionRatio().add(Double.parseDouble(d.trim()));
                }
            }

            // 提取个人持有比例
            Pattern indvP = Pattern.compile("\"name\":\"个人持有比例\",\"data\":\\[([0-9.,]+)\\]");
            Matcher indvM = indvP.matcher(jsonStr);
            if (indvM.find()) {
                String[] data = indvM.group(1).split(",");
                hs.setIndividualRatio(new ArrayList<>());
                for (String d : data) {
                    hs.getIndividualRatio().add(Double.parseDouble(d.trim()));
                }
            }

            // 提取内部持有比例
            Pattern intP = Pattern.compile("\"name\":\"内部持有比例\",\"data\":\\[([0-9.,]+)\\]");
            Matcher intM = intP.matcher(jsonStr);
            if (intM.find()) {
                String[] data = intM.group(1).split(",");
                hs.setInternalRatio(new ArrayList<>());
                for (String d : data) {
                    hs.getInternalRatio().add(Double.parseDouble(d.trim()));
                }
            }
        }
        return hs;
    }

    // ==================== 11. 统一数据集合 ====================

    @Data
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
        private List<NavData> mergedWorthTrend;  // 合并后的净值数据

        // 其他数据
        private List<PositionData> sharesPositions;
        private List<GrandTotalData> grandTotal;
        private AssetAllocation assetAllocation;
        private List<FundManager> fundManagers;
        private PerformanceEvaluation performanceEvaluation;
        private FluctuationScale fluctuationScale;
        private HolderStructure holderStructure;
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
        List<NavData> netWorthList = parseNetWorthTrend(content);
        List<NavData> accWorthList = parseACWorthTrend(content);
        fundData.setNetWorthTrend(netWorthList);
        fundData.setAccumulatedWorthTrend(accWorthList);
        fundData.setMergedWorthTrend(mergeNavData(netWorthList, accWorthList));

        // 5. 其他数据
        fundData.setSharesPositions(parseSharesPositions(content));
        fundData.setGrandTotal(parseGrandTotal(content));
        fundData.setAssetAllocation(parseAssetAllocation(content));
        fundData.setFundManagers(parseCurrentFundManager(content));
        fundData.setPerformanceEvaluation(parsePerformanceEvaluation(content));
        fundData.setFluctuationScale(parseFluctuationScale(content));
        fundData.setHolderStructure(parseHolderStructure(content));

        return fundData;
    }
}