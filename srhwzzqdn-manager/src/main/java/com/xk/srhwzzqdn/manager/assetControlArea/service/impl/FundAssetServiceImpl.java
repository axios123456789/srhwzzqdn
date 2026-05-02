package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.xk.srhwzzqdn.manager.assetControlArea.mapper.FundAssetMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.FundAssetService;
import com.xk.srhwzzqdn.manager.assetControlArea.util.FundDataParser;
import com.xk.srhwzzqdn.manager.system.mapper.SysDictMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 基金资产管理服务实现类
 * 负责基金数据的获取、解析和入库
 *
 * @author system
 * @date 2026-04-26
 */
@Service
public class FundAssetServiceImpl implements FundAssetService {

    /** 基金资产数据访问层，用于数据库操作 */
    @Autowired
    private FundAssetMapper fundAssetMapper;

    /** 系统字典数据访问层，用于获取配置信息（如接口URL） */
    @Autowired
    private SysDictMapper sysDictMapper;

    /**
     * 获取基金测试数据
     * 从东方财富接口拉取基金数据，解析后打印到控制台
     *
     * @param fundCode 基金代码，如 "460001" 表示华泰柏瑞盛世中国混合
     */
    @Override
    public void getFundTestData(String fundCode) {
        // 1. 获取基金基本数据获取的url配置
        // 从系统字典表查询配置的接口地址模板，例如: "https://fund.eastmoney.com/pingzhongdata/{fund_code}.js"
        String fund_base_url = sysDictMapper.getConfigValueById("get_fund_base_data_url");
        // 将URL模板中的占位符 {fund_code} 替换为实际的基金代码
        String fund_url = fund_base_url.replace("{fund_code}", fundCode);
        // 发送HTTP请求获取接口返回的JS文件内容
        String fund_content = fetchFundData(fund_url);

        if (fund_content != null) {
            // 2. 解析完整基金数据
            // 使用自定义解析工具类将JS格式的数据转换为Java对象
            FundDataParser.FundData fundData = FundDataParser.parseAllFundData(fund_content);

            // 3. 打印到控制台（用于调试和查看解析结果）
            printFundData(fundData);

            // 4. TODO: 数据入库逻辑
            // 后续在此处添加将解析后的基金数据保存到数据库的代码
            // saveToDatabase(fundData);

        } else {
            // 接口请求失败或返回内容为空时打印错误信息
            System.err.println("获取基金数据失败，fundCode: " + fundCode);
        }
    }

    /**
     * 打印基金数据到控制台
     * 按模块分类输出解析结果，便于查看和调试
     *
     * @param fundData 解析后的基金数据对象
     */
    private void printFundData(FundDataParser.FundData fundData) {
        // 输出分隔线和解析时间
        System.out.println("\n==================== 基金数据解析结果 ====================");
        System.out.println("解析时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // 依次打印各个模块的数据
        printBasicInfo(fundData);          // 一、基础信息
        printReturns(fundData);             // 二、收益率
        printStockHoldings(fundData);       // 三、持仓股票
        printNavData(fundData);             // 四、净值数据（核心）
        printAssetAllocation(fundData);     // 五、资产配置
        printFundManagers(fundData);        // 六、基金经理
        printPerformanceEvaluation(fundData); // 七、业绩评价
        printFluctuationScale(fundData);    // 八、规模变动
        printHolderStructure(fundData);     // 九、持有人结构

        // 结束分隔线
        System.out.println("\n==================== 解析完成 ====================\n");
    }

    /**
     * 打印基础信息
     * 包括基金名称、代码、申购费率等
     *
     * @param fundData 基金数据对象
     */
    private void printBasicInfo(FundDataParser.FundData fundData) {
        System.out.println("\n【一、基础信息】");
        System.out.println("  基金名称: " + fundData.getFundName());      // 基金完整名称
        System.out.println("  基金代码: " + fundData.getFundCode());      // 基金唯一标识代码
        System.out.println("  原申购费率: " + fundData.getSourceRate() + "%");  // 标准申购费率（如1.5%）
        System.out.println("  现申购费率: " + fundData.getCurrentRate() + "%"); // 当前优惠费率（如一折后0.15%）
    }

    /**
     * 打印收益率信息
     * 包括近1月、近3月、近6月、近1年的收益率
     *
     * @param fundData 基金数据对象
     */
    private void printReturns(FundDataParser.FundData fundData) {
        System.out.println("\n【二、收益率】");
        System.out.println("  近1月收益率: " + fundData.getReturn1Month() + "%");  // 过去1个月的收益率
        System.out.println("  近3月收益率: " + fundData.getReturn3Month() + "%");  // 过去3个月的收益率
        System.out.println("  近6月收益率: " + fundData.getReturn6Month() + "%");  // 过去6个月的收益率
        System.out.println("  近1年收益率: " + fundData.getReturn1Year() + "%");   // 过去1年的收益率
    }

    /**
     * 打印持仓股票信息
     * 展示基金的前十大重仓股代码及所属市场
     *
     * @param fundData 基金数据对象
     */
    private void printStockHoldings(FundDataParser.FundData fundData) {
        System.out.println("\n【三、持仓股票（前十大重仓股）】");

        // 方式一：使用原有的 stockCodesNew 列表，手动转换
        List<String> stockCodes = fundData.getStockCodesNew();
        if (stockCodes != null && !stockCodes.isEmpty()) {
            System.out.println("  【方式一：编码转换】");
            for (int i = 0; i < stockCodes.size(); i++) {
                String encodedCode = stockCodes.get(i);
                String realCode = FundDataParser.decodeStockCode(encodedCode);
                String stockName = FundDataParser.getStockName(realCode);
                String market = FundDataParser.getStockMarket(encodedCode);
                System.out.printf("  %2d. %s -> %s (%s) - %s%n",
                        i + 1, encodedCode, realCode, market,
                        stockName.isEmpty() ? "待补充" : stockName);
            }
        } else {
            System.out.println("  无持仓数据");
        }

        // 方式二：使用新增的 stockInfoList（更简洁）
        List<FundDataParser.StockInfo> stockInfoList = fundData.getStockInfoList();
        if (stockInfoList != null && !stockInfoList.isEmpty()) {
            System.out.println("\n  【方式二：使用StockInfo对象】");
            for (int i = 0; i < stockInfoList.size(); i++) {
                FundDataParser.StockInfo info = stockInfoList.get(i);
                System.out.printf("  %2d. %s%n", i + 1, info);
            }
        }
    }

    /**
     * 打印净值数据（核心数据）
     * 包括单位净值走势、累计净值、最新净值等
     *
     * @param fundData 基金数据对象
     */
    private void printNavData(FundDataParser.FundData fundData) {
        // 获取单位净值走势数据（日度数据）
        List<FundDataParser.NavData> navList = fundData.getNetWorthTrend();
        // 获取合并后的净值数据（包含单位净值和累计净值）
        List<FundDataParser.NavData> mergedList = fundData.getMergedWorthTrend();

        System.out.println("\n【四、净值数据】");
        System.out.println("  单位净值记录数: " + (navList != null ? navList.size() : 0) + " 条");

        if (navList != null && !navList.isEmpty()) {
            // 打印最早3条记录（了解基金成立初期净值）
            System.out.println("\n  --- 最早3条记录 ---");
            for (int i = 0; i < Math.min(3, navList.size()); i++) {
                System.out.println("    " + formatNavData(navList.get(i)));
            }

            // 打印最新5条记录（了解近期净值变化）
            System.out.println("\n  --- 最新5条记录 ---");
            int size = navList.size();
            for (int i = Math.max(0, size - 5); i < size; i++) {
                System.out.println("    " + formatNavData(navList.get(i)));
            }

            // 打印最新净值摘要
            FundDataParser.NavData latestNav = navList.get(size - 1);
            System.out.println("\n  【最新净值摘要】");
            System.out.println("    最新净值日期: " + formatDate(latestNav.getDate()));  // 数据截止日期
            System.out.println("    最新单位净值: " + latestNav.getNav());               // 每份基金的价值
            if (latestNav.getDailyReturn() != null) {
                System.out.println("    日涨跌幅: " + latestNav.getDailyReturn() + "%"); // 当日涨跌百分比
            }
        }

        // 打印累计净值（真实历史总回报）
        FundDataParser.NavData latestAccNav = getLatestWithAccumulatedNav(mergedList);
        if (latestAccNav != null && latestAccNav.getAccumulatedNav() != null) {
            System.out.println("\n  【累计净值】");
            System.out.println("    最新累计净值: " + latestAccNav.getAccumulatedNav());
            System.out.println("    日期: " + formatDate(latestAccNav.getDate()));
        }
    }

    /**
     * 获取最新的有累计净值的记录
     * 从合并净值列表中查找最后一条包含累计净值的记录
     *
     * @param list 合并后的净值列表
     * @return 最新的包含累计净值的记录，如果没有则返回null
     */
    private FundDataParser.NavData getLatestWithAccumulatedNav(List<FundDataParser.NavData> list) {
        if (list == null) return null;
        // 从后往前遍历，找到第一条有累计净值的记录
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).getAccumulatedNav() != null) {
                return list.get(i);
            }
        }
        return null;
    }

    /**
     * 打印资产配置信息（完善版：支持其他资产和基金占比）
     * 包括净资产规模、股票/债券/现金/其他/基金占比等
     *
     * @param fundData 基金数据对象
     */
    private void printAssetAllocation(FundDataParser.FundData fundData) {
        FundDataParser.AssetAllocation aa = fundData.getAssetAllocation();
        if (aa != null) {
            System.out.println("\n【五、资产配置】");

            // 净资产规模（基金管理的总资产）
            if (aa.getNetAssets() != null && !aa.getNetAssets().isEmpty()) {
                System.out.println("  净资产规模: " + aa.getNetAssets() + " 亿元");
            }

            // 获取最新一期索引
            int lastIdx = -1;
            if (aa.getStockRatio() != null && !aa.getStockRatio().isEmpty()) {
                lastIdx = aa.getStockRatio().size() - 1;
            } else if (aa.getBondRatio() != null && !aa.getBondRatio().isEmpty()) {
                lastIdx = aa.getBondRatio().size() - 1;
            } else if (aa.getCashRatio() != null && !aa.getCashRatio().isEmpty()) {
                lastIdx = aa.getCashRatio().size() - 1;
            } else if (aa.getFundRatio() != null && !aa.getFundRatio().isEmpty()) {
                lastIdx = aa.getFundRatio().size() - 1;
            }

            if (lastIdx >= 0) {
                // 输出所有非空的资产占比
                if (aa.getStockRatio() != null && !aa.getStockRatio().isEmpty()) {
                    System.out.printf("  最新股票占比: %.2f%%%n", aa.getStockRatio().get(Math.min(lastIdx, aa.getStockRatio().size() - 1)));
                }
                if (aa.getBondRatio() != null && !aa.getBondRatio().isEmpty()) {
                    System.out.printf("  最新债券占比: %.2f%%%n", aa.getBondRatio().get(Math.min(lastIdx, aa.getBondRatio().size() - 1)));
                }
                if (aa.getCashRatio() != null && !aa.getCashRatio().isEmpty()) {
                    System.out.printf("  最新现金占比: %.2f%%%n", aa.getCashRatio().get(Math.min(lastIdx, aa.getCashRatio().size() - 1)));
                }
                if (aa.getFundRatio() != null && !aa.getFundRatio().isEmpty()) {
                    System.out.printf("  最新基金占比: %.2f%%%n", aa.getFundRatio().get(Math.min(lastIdx, aa.getFundRatio().size() - 1)));
                }
                if (aa.getGoldRatio() != null && !aa.getGoldRatio().isEmpty()) {
                    System.out.printf("  最新黄金占比: %.2f%%%n", aa.getGoldRatio().get(Math.min(lastIdx, aa.getGoldRatio().size() - 1)));
                }
                if (aa.getOtherRatio() != null && !aa.getOtherRatio().isEmpty()) {
                    System.out.printf("  最新其他资产占比: %.2f%%%n", aa.getOtherRatio().get(Math.min(lastIdx, aa.getOtherRatio().size() - 1)));
                }
            } else {
                // 如果没有找到任何资产配置数据，尝试从series中打印所有
                if (aa.getSeries() != null && !aa.getSeries().isEmpty()) {
                    System.out.println("  资产配置明细:");
                    for (Map.Entry<String, List<Double>> entry : aa.getSeries().entrySet()) {
                        List<Double> values = entry.getValue();
                        if (values != null && !values.isEmpty()) {
                            System.out.printf("    %s: %.2f%%%n", entry.getKey(), values.get(values.size() - 1));
                        }
                    }
                }
            }
        }
    }

    /**
     * 打印基金经理信息（修复版：格式化从业年限）
     * 包括姓名、星级、从业时间、管理规模等
     *
     * @param fundData 基金数据对象
     */
    private void printFundManagers(FundDataParser.FundData fundData) {
        List<FundDataParser.FundManager> managers = fundData.getFundManagers();
        if (managers != null && !managers.isEmpty()) {
            System.out.println("\n【六、基金经理】");
            for (FundDataParser.FundManager fm : managers) {
                System.out.println("  姓名: " + fm.getName());                    // 基金经理姓名
                System.out.println("  星级: " + fm.getStar() + "星");            // 基金经理星级评价（1-5星）
                // 将 "12年又112天" 格式转换为 "12.3年" 格式
                String formattedWorkTime = FundDataParser.convertWorkTimeToYears(fm.getWorkTime());
                System.out.println("  从业时间: " + (formattedWorkTime != null ? formattedWorkTime : "未知"));
                System.out.println("  管理规模: " + (fm.getFundSize() != null ? fm.getFundSize() : "未知"));
                System.out.println("  ---");
            }
        } else {
            System.out.println("\n【六、基金经理】无数据");
        }
    }

    /**
     * 打印业绩评价
     * 包括综合评分和各项能力得分（选证能力、收益率、抗风险、稳定性、择时能力）
     *
     * @param fundData 基金数据对象
     */
    private void printPerformanceEvaluation(FundDataParser.FundData fundData) {
        FundDataParser.PerformanceEvaluation pe = fundData.getPerformanceEvaluation();
        if (pe != null && pe.getScores() != null && !pe.getScores().isEmpty()) {
            System.out.println("\n【七、业绩评价】");
            System.out.println("  综合评分: " + pe.getAvgScore() + "分");  // 整体评价得分
            System.out.println("  各项得分:");
            if (pe.getCategories() != null && pe.getScores() != null &&
                    pe.getCategories().size() == pe.getScores().size()) {
                for (int i = 0; i < pe.getCategories().size(); i++) {
                    // 各项能力得分：选证能力、收益率、抗风险、稳定性、择时能力
                    System.out.printf("    %s: %.1f分%n", pe.getCategories().get(i), pe.getScores().get(i));
                }
            }
        }
    }

    /**
     * 打印规模变动信息
     * 展示基金历史净资产规模及环比变化
     *
     * @param fundData 基金数据对象
     */
    private void printFluctuationScale(FundDataParser.FundData fundData) {
        FundDataParser.FluctuationScale fs = fundData.getFluctuationScale();
        if (fs != null && fs.getScale() != null && !fs.getScale().isEmpty()) {
            System.out.println("\n【八、规模变动】");
            System.out.println("  最新规模: " + fs.getScale().get(fs.getScale().size() - 1) + " 亿元");
            if (fs.getCategories() != null && fs.getCategories().size() == fs.getScale().size()) {
                System.out.println("  规模历史:");
                for (int i = 0; i < fs.getCategories().size(); i++) {
                    // categories: 报告期（如2024-12-31）
                    // scale: 对应报告期的资产规模
                    // mom: 较上期环比变化百分比
                    System.out.printf("    %s: %.2f 亿元 (环比: %s)%n",
                            fs.getCategories().get(i), fs.getScale().get(i),
                            fs.getMom() != null && i < fs.getMom().size() ? fs.getMom().get(i) : "-");
                }
            }
        }
    }

    /**
     * 打印持有人结构信息
     * 展示机构、个人、内部持有比例
     *
     * @param fundData 基金数据对象
     */
    private void printHolderStructure(FundDataParser.FundData fundData) {
        FundDataParser.HolderStructure hs = fundData.getHolderStructure();
        if (hs != null && hs.getInstitutionRatio() != null && !hs.getInstitutionRatio().isEmpty()) {
            System.out.println("\n【九、持有人结构】");
            int lastIdx = hs.getInstitutionRatio().size() - 1;  // 最新一期索引
            System.out.printf("  机构持有比例: %.2f%%%n", hs.getInstitutionRatio().get(lastIdx));  // 机构投资者占比
            System.out.printf("  个人持有比例: %.2f%%%n", hs.getIndividualRatio().get(lastIdx));  // 个人投资者占比
            if (hs.getInternalRatio() != null && lastIdx < hs.getInternalRatio().size()) {
                System.out.printf("  内部持有比例: %.2f%%%n", hs.getInternalRatio().get(lastIdx)); // 基金公司内部持有占比
            }
        }
    }

    /**
     * 获取URL返回内容
     * 发送HTTP GET请求，获取接口返回的JS文件内容
     *
     * @param url 请求地址
     * @return 接口返回的HTML/JS内容，失败时返回null
     */
    public static String fetchFundData(String url) {
        // 使用try-with-resources自动关闭HTTP客户端和响应
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // 创建GET请求
            HttpGet request = new HttpGet(url);
            // 设置User-Agent模拟浏览器访问，避免被服务器拒绝
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            try (CloseableHttpResponse response = client.execute(request)) {
                // 获取响应状态码
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // 成功时返回响应内容（UTF-8编码）
                    return EntityUtils.toString(response.getEntity(), "UTF-8");
                } else {
                    // 失败时打印错误状态码
                    System.err.println("请求失败，HTTP状态码: " + statusCode);
                }
            }
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 格式化净值数据为可读字符串
     *
     * @param nav 净值数据对象
     * @return 格式化后的字符串，包含日期、净值、日涨跌幅、分红信息
     */
    private String formatNavData(FundDataParser.NavData nav) {
        StringBuilder sb = new StringBuilder();
        sb.append("日期: ").append(formatDate(nav.getDate()));      // 净值日期
        sb.append(", 净值: ").append(nav.getNav());                  // 单位净值
        if (nav.getDailyReturn() != null) {
            sb.append(", 日涨跌幅: ").append(nav.getDailyReturn()).append("%");  // 日涨跌幅
        }
        if (nav.getDividendInfo() != null && !nav.getDividendInfo().isEmpty()) {
            sb.append(", ").append(nav.getDividendInfo());           // 分红或拆分信息
        }
        return sb.toString();
    }

    /**
     * 格式化日期
     * 将Date对象转换为 yyyy-MM-dd 格式的字符串
     *
     * @param date 日期对象
     * @return 格式化后的日期字符串，若date为null返回"未知"
     */
    private String formatDate(Date date) {
        if (date == null) return "未知";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}