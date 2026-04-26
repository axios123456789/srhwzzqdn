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

@Service
public class FundAssetServiceImpl implements FundAssetService {
    @Autowired
    private FundAssetMapper fundAssetMapper;
    @Autowired
    private SysDictMapper sysDictMapper;

    /**
     * 获取基金基本数据
     * @param fundCode
     */
    @Override
    public void getFundBaseData(String fundCode) {
        //1.获取基金基本数据获取的url配置
        String fund_base_url = sysDictMapper.getConfigValueById("get_fund_base_data_url");
        String fund_url = fund_base_url.replace("{fund_code}", fundCode);
        String fund_content = fetchFundData(fund_url);
        if (fund_content != null) {
            // 2. 解析完整基金数据
            FundDataParser.FundData fundData = FundDataParser.parseAllFundData(fund_content);

            // 3. 打印到控制台（开始分隔线）
            System.out.println("\n==================== 基金数据解析结果 ====================");
            System.out.println("解析时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            // 3.1 打印基础信息
            System.out.println("\n【一、基础信息】");
            System.out.println("  基金名称: " + fundData.getFundName());
            System.out.println("  基金代码: " + fundData.getFundCode());
            System.out.println("  原申购费率: " + fundData.getSourceRate() + "%");
            System.out.println("  现申购费率: " + fundData.getCurrentRate() + "%");

            // 3.2 打印收益率
            System.out.println("\n【二、收益率】");
            System.out.println("  近1月收益率: " + fundData.getReturn1Month() + "%");
            System.out.println("  近3月收益率: " + fundData.getReturn3Month() + "%");
            System.out.println("  近6月收益率: " + fundData.getReturn6Month() + "%");
            System.out.println("  近1年收益率: " + fundData.getReturn1Year() + "%");

            // 3.3 打印持仓股票
            System.out.println("\n【三、持仓股票（前十大重仓股）】");
            List<String> stockCodes = fundData.getStockCodesNew();
            if (stockCodes != null && !stockCodes.isEmpty()) {
                for (int i = 0; i < stockCodes.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + stockCodes.get(i));
                }
            } else {
                System.out.println("  无持仓数据");
            }

            // 3.4 打印净值数据（最重要）
            List<FundDataParser.NavData> navList = fundData.getNetWorthTrend();
            System.out.println("\n【四、单位净值走势数据】（共 " + navList.size() + " 条记录）");

            if (navList != null && !navList.isEmpty()) {
                // 打印前5条
                System.out.println("\n  --- 前5条记录 ---");
                for (int i = 0; i < Math.min(5, navList.size()); i++) {
                    FundDataParser.NavData nav = navList.get(i);
                    System.out.println("    " + formatNavData(nav));
                }

                // 打印后5条（最新数据）
                System.out.println("\n  --- 后5条记录（最新数据）---");
                int size = navList.size();
                for (int i = Math.max(0, size - 5); i < size; i++) {
                    FundDataParser.NavData nav = navList.get(i);
                    System.out.println("    " + formatNavData(nav));
                }

                // 打印最新净值摘要
                FundDataParser.NavData latestNav = navList.get(size - 1);
                System.out.println("\n  【最新净值摘要】");
                System.out.println("    最新净值日期: " + formatDate(latestNav.getDate()));
                System.out.println("    最新单位净值: " + latestNav.getNav());
                if (latestNav.getDailyReturn() != null) {
                    System.out.println("    日涨跌幅: " + latestNav.getDailyReturn() + "%");
                }
            }

            // 3.5 打印累计净值数据
            List<FundDataParser.NavData> accNavList = fundData.getAccumulatedWorthTrend();
            if (accNavList != null && !accNavList.isEmpty()) {
                System.out.println("\n【五、累计净值走势】（共 " + accNavList.size() + " 条记录）");
                FundDataParser.NavData latestAccNav = accNavList.get(accNavList.size() - 1);
                System.out.println("  最新累计净值: " + latestAccNav.getAccumulatedNav() + "（日期: " + formatDate(latestAccNav.getDate()) + "）");
            }

            // 3.6 打印资产配置
            FundDataParser.AssetAllocation assetAllocation = fundData.getAssetAllocation();
            if (assetAllocation != null && assetAllocation.getNetAssets() != null && !assetAllocation.getNetAssets().isEmpty()) {
                System.out.println("\n【六、资产配置】");
                List<Double> netAssets = assetAllocation.getNetAssets();
                System.out.println("  净资产规模: " + netAssets + " 亿元");
            }

            // 3.7 打印基金经理信息
            List<FundDataParser.FundManager> managers = fundData.getFundManagers();
            if (managers != null && !managers.isEmpty()) {
                System.out.println("\n【七、基金经理】");
                for (FundDataParser.FundManager fm : managers) {
                    System.out.println("  姓名: " + fm.getName());
                    System.out.println("  星级: " + fm.getStar() + "星");
                    System.out.println("  从业时间: " + fm.getWorkTime());
                    System.out.println("  管理规模: " + fm.getFundSize());
                }
            }

            // 3.8 打印业绩评价
            Map<String, Object> perfEval = fundData.getPerformanceEvaluation();
            if (perfEval != null && !perfEval.isEmpty()) {
                System.out.println("\n【八、业绩评价】");
                System.out.println("  综合评分: " + perfEval.get("avgScore") + "分");
                @SuppressWarnings("unchecked")
                List<Double> scores = (List<Double>) perfEval.get("scores");
                @SuppressWarnings("unchecked")
                List<String> categories = (List<String>) perfEval.get("categories");
                if (scores != null && categories != null && scores.size() == categories.size()) {
                    System.out.println("  各项得分:");
                    for (int i = 0; i < categories.size(); i++) {
                        System.out.println("    " + categories.get(i) + ": " + scores.get(i) + "分");
                    }
                }
            }

            System.out.println("\n==================== 解析完成 ====================\n");
        } else {
            System.err.println("获取基金数据失败，fundCode: " + fundCode);
        }
    }

    /**
     * 获取url返回内容
     * @param url
     * @return
     */
    public static String fetchFundData(String url) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            // 设置一个常见的 User-Agent，避免被服务器拒绝
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    return EntityUtils.toString(response.getEntity(), "UTF-8");
                } else {
                    System.err.println("请求失败，HTTP状态码: " + statusCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 格式化净值数据显示
     */
    private String formatNavData(FundDataParser.NavData nav) {
        StringBuilder sb = new StringBuilder();
        sb.append("日期: ").append(formatDate(nav.getDate()));
        sb.append(", 净值: ").append(nav.getNav());
        if (nav.getDailyReturn() != null) {
            sb.append(", 日涨跌幅: ").append(nav.getDailyReturn()).append("%");
        }
        if (nav.getDividendInfo() != null && !nav.getDividendInfo().isEmpty()) {
            sb.append(", ").append(nav.getDividendInfo());
        }
        return sb.toString();
    }

    /**
     * 格式化日期
     */
    private String formatDate(Date date) {
        if (date == null) return "未知";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
