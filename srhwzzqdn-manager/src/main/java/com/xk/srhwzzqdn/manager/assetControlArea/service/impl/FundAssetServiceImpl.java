package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.assetControlArea.mapper.AssetLedgerMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.mapper.FundAssetMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.FundAssetService;
import com.xk.srhwzzqdn.manager.assetControlArea.util.FundDataParser;
import com.xk.srhwzzqdn.manager.system.mapper.SysDictMapper;
import com.xk.srhwzzqdn.manager.util.BailianApiUtil;
import com.xk.srhwzzqdn.model.dto.assetControl.FundBaseDateDto;
import com.xk.srhwzzqdn.model.dto.assetControl.FundComm;
import com.xk.srhwzzqdn.model.entity.assetControl.*;
import com.xk.srhwzzqdn.model.vo.assetControl.NavEChartsVo;
import com.xk.srhwzzqdn.model.vo.assetControl.PerformanceAnalysisVo;
import com.xk.srhwzzqdn.util.AuthContextUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static final Logger logger = LoggerFactory.getLogger(FundAssetService.class);

    /** 基金资产数据访问层，用于数据库操作 */
    @Autowired
    private FundAssetMapper fundAssetMapper;

    /** 资产台账数据访问层 */
    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    //@Autowired
    //private DeepSeekApiUtil deepSeekApiUtil;
    @Autowired
    private BailianApiUtil bailianApiUtil;

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
     * 获取基金基本数据，基金经理基本数据，关联持仓数据获取
     * 不做更新处理，只做一次性获取数据
     * @param fundCode
     * @return
     */
    @Override
    @Transactional
    public String getFundBaseDataByCode(String fundCode) {
        //1.根据基金代码查询基金的数据是否已获取
        int isExist = fundAssetMapper.isExistByCode(fundCode);
        if (isExist > 0) {
            //logger.error("基金数据已存在，请勿重复获取！");
            return "基金数据已存在，请勿重复获取！";
        }

        // 2. 获取基金基本数据获取的url配置
        // 从系统字典表查询配置的接口地址模板，例如: "https://fund.eastmoney.com/pingzhongdata/{fund_code}.js"
        String fund_base_url = sysDictMapper.getConfigValueById("get_fund_base_data_url");
        // 将URL模板中的占位符 {fund_code} 替换为实际的基金代码
        String fund_url = fund_base_url.replace("{fund_code}", fundCode);
        // 发送HTTP请求获取接口返回的JS文件内容
        String fund_content = fetchFundData(fund_url);

        // 3.创建基金基本对象
        FundAsset fundAsset = new FundAsset();

        // 3.1. 解析完整基金数据
        // 使用自定义解析工具类将JS格式的数据转换为Java对象
        FundDataParser.FundData fundData = FundDataParser.parseAllFundData(fund_content);

        //4.从天天基金接口解析数据到基金对象中
        if (fundData.getFundCode() != null) {
            //4.2.设置从天天基金接口获取到的基金基本数据
            fundAsset.setFundCode(fundData.getFundCode());//基金代码
            fundAsset.setFundName(fundData.getFundName());//基金名称
            fundAsset.setPurchaseFeeRate(new BigDecimal(fundData.getCurrentRate()).setScale(2, java.math.RoundingMode.HALF_UP));//申购费率
            fundAsset.setReturn1m(new BigDecimal(fundData.getReturn1Month()).setScale(2, java.math.RoundingMode.HALF_UP));//近一月收益率
            fundAsset.setReturn3m(new BigDecimal(fundData.getReturn3Month()).setScale(2, java.math.RoundingMode.HALF_UP));//近三月收益率
            fundAsset.setReturn6m(new BigDecimal(fundData.getReturn6Month()).setScale(2, java.math.RoundingMode.HALF_UP));//近六月收益率
            fundAsset.setReturn1y(new BigDecimal(fundData.getReturn1Year()).setScale(2, java.math.RoundingMode.HALF_UP));//近一年收益率
            fundAsset.setNetAssets(fundData.getAssetAllocation().getNetAssets().toString()+"（亿元）");
            fundAsset.setStockRatio(new BigDecimal(fundData.getAssetAllocation().getStockRatio().get(Math.min(fundData.getAssetAllocation().getStockRatio().size() - 1, fundData.getAssetAllocation().getStockRatio().size() - 1))).setScale(2, java.math.RoundingMode.HALF_UP));//最新股票占比
            fundAsset.setBondRatio(new BigDecimal(fundData.getAssetAllocation().getBondRatio().get(Math.min(fundData.getAssetAllocation().getBondRatio().size() - 1, fundData.getAssetAllocation().getBondRatio().size() - 1))).setScale(2, java.math.RoundingMode.HALF_UP));//最新债券占比
            fundAsset.setCashRatio(new BigDecimal(fundData.getAssetAllocation().getCashRatio().get(Math.min(fundData.getAssetAllocation().getCashRatio().size() - 1, fundData.getAssetAllocation().getCashRatio().size() - 1))).setScale(2, java.math.RoundingMode.HALF_UP));//最新现金占比
            fundAsset.setLatestScale(new BigDecimal(fundData.getFluctuationScale().getScale().get(fundData.getFluctuationScale().getScale().size() - 1)).setScale(2, java.math.RoundingMode.HALF_UP));//最新规模
            fundAsset.setAssetScale(new BigDecimal(fundData.getFluctuationScale().getScale().get(fundData.getFluctuationScale().getScale().size() - 1)).setScale(2, java.math.RoundingMode.HALF_UP));//资产规模
            //设置规模历史
            StringBuilder historyScale = new StringBuilder();
            historyScale.append("  历史规模: ");
            for (int i = 0; i < fundData.getFluctuationScale().getCategories().size(); i++) {
                historyScale.append(String.format("%n    %s: %.2f 亿元 (环比: %s)",
                        fundData.getFluctuationScale().getCategories().get(i),
                        fundData.getFluctuationScale().getScale().get(i),
                        fundData.getFluctuationScale().getMom() != null && i < fundData.getFluctuationScale().getMom().size() ? fundData.getFluctuationScale().getMom().get(i) : "-"));
            }
            fundAsset.setScaleHistory(historyScale.toString());
            //设置持有人结构
            fundAsset.setInstitutionRatio(new BigDecimal(fundData.getHolderStructure().getInstitutionRatio().get(fundData.getHolderStructure().getInstitutionRatio().size() - 1)).setScale(2, java.math.RoundingMode.HALF_UP));// 机构投资者占比
            fundAsset.setIndividualRatio(new BigDecimal(fundData.getHolderStructure().getIndividualRatio().get(fundData.getHolderStructure().getInstitutionRatio().size() - 1)).setScale(2, java.math.RoundingMode.HALF_UP));// 个人投资者占比
            fundAsset.setInternalRatio(new BigDecimal(fundData.getHolderStructure().getInternalRatio().get(fundData.getHolderStructure().getInstitutionRatio().size() - 1)).setScale(2, java.math.RoundingMode.HALF_UP)); // 基金公司内部持有占比
            //设置基金经理名称
            fundAsset.setFundManager(fundData.getFundManagers().get(fundData.getFundManagers().size() - 1).getName());
            //设置基金成立日期
            fundAsset.setEstablishDate(fundData.getNetWorthTrend().get(0).getDate());
        } else {
            // 接口请求失败或返回内容为空时打印错误信息
            logger.error("获取基金数据失败，fundCode: ", fundCode);
            return "无"+fundCode+"这个代码的基金！";
        }

        //5.调用deepSeek Ai或其他Ai模型补充基金的其他信息
        fundAsset.setFundCompany(bailianApiUtil.call(fundAsset.getFundName() + "（"+ fundCode + "）" + "（仅输出基金管理人）")); //基金管理人
        fundAsset.setFundCompanyDesc(bailianApiUtil.call(fundAsset.getFundCompany() + "仅针对该基金管理人实时准确简洁重点描述（输出一段不多于100个字的描述）")); //基金管理人描述
        fundAsset.setCustodian(bailianApiUtil.call(fundAsset.getFundName() + "（仅输出基金托管者）"));//基金托管者
        fundAsset.setManagerDesc(bailianApiUtil.call(fundAsset.getFundManager() + "仅针对该基金经理实时准确简洁重点描述（仅输出一段不多于300个字的描述）"));//基金经理描述
        fundAsset.setTradeRule(bailianApiUtil.call(fundAsset.getFundName() + "仅输出基金交易规则（用简短的一段话）")); //交易规则
        fundAsset.setCreateBy("system");
        //System.out.println("获取到的基金基本信息"+fundAsset.toString());

        //6.创建基金经理基本对象
        FundManagerAnalysis fundManagerAnalysis = new FundManagerAnalysis();

        //7.从天天基金接口获取数据到基金经理分析对象中
        fundManagerAnalysis.setFundCode(fundCode); //基金代码
        fundManagerAnalysis.setManagerName(fundData.getFundManagers().get(fundData.getFundManagers().size() - 1).getName()); //基金经理名称
        fundManagerAnalysis.setStarRating(new BigDecimal(fundData.getFundManagers().get(fundData.getFundManagers().size() - 1).getStar())); //基金经理评分
        
        // 处理从业时间：去除"年"字后转换为BigDecimal
        String workTimeStr = FundDataParser.convertWorkTimeToYears(fundData.getFundManagers().get(fundData.getFundManagers().size() - 1).getWorkTime());
        workTimeStr = workTimeStr.replace("年", "");
        fundManagerAnalysis.setWorkTime(new BigDecimal(workTimeStr));//基金经理从业时间
        
        // 处理基金规模：去除"亿元"等单位后转换为BigDecimal
        String fundSizeStr = fundData.getFundManagers().get(fundData.getFundManagers().size() - 1).getFundSize();
        fundSizeStr = fundSizeStr.replaceAll("[^0-9.-]", "");
        if (!fundSizeStr.isEmpty()) {
            fundManagerAnalysis.setManageScale(new BigDecimal(fundSizeStr));//基金规模
        }
        
        //从性能评价数据中提取各项评分
        FundDataParser.PerformanceEvaluation pe = fundData.getPerformanceEvaluation();
        if (pe != null) {
            // 设置综合评分（平均分）
            if (pe.getAvgScore() > 0) {
                fundManagerAnalysis.setTotalScore(new BigDecimal(pe.getAvgScore()).setScale(2, java.math.RoundingMode.HALF_UP));
            }
            
            // 根据分类名称设置各项能力评分
            if (pe.getCategories() != null && pe.getScores() != null && pe.getCategories().size() == pe.getScores().size()) {
                for (int i = 0; i < pe.getCategories().size(); i++) {
                    String category = pe.getCategories().get(i);
                    BigDecimal score = new BigDecimal(pe.getScores().get(i)).setScale(2, java.math.RoundingMode.HALF_UP);
                    
                    // 根据分类名称匹配对应的评分字段（使用else-if确保每个字段只设置一次）
                    if (category.contains("选证") || category.contains("选股")) {
                        fundManagerAnalysis.setStockSelectScore(score);
                    } else if (category.contains("收益") || category.contains("回报")) {
                        fundManagerAnalysis.setReturnScore(score);
                    } else if (category.contains("风险") || category.contains("抗风险")) {
                        fundManagerAnalysis.setRiskControlScore(score);
                    } else if (category.contains("稳定") || category.contains("波动")) {
                        fundManagerAnalysis.setStabilityScore(score);
                    } else if (category.contains("择时")) {
                        fundManagerAnalysis.setTimingScore(score);
                    }
                    // else-if结构保证了即使有多个匹配的分类，每个字段也只会被设置一次
                    // 后面的分类不会覆盖前面已设置的同类型字段
                }
            }
        }
        fundManagerAnalysis.setManagerDesc(fundAsset.getManagerDesc()); //基金经理描述
        fundManagerAnalysis.setConcentrationRateAnalyse(bailianApiUtil.call(fundAsset.getFundName()+"对这个基金的持仓集中度进行分析（输出一段300字以内的实时准确直击重点的简洁描述）"));
        fundManagerAnalysis.setTurnoverRateAnalyse(bailianApiUtil.call(fundAsset.getFundName()+"针对这个基金的换手率进行分析（输出一段300字以内的实时准确直击重点的简洁描述）"));
        fundManagerAnalysis.setCapabilityPathAnalysis(bailianApiUtil.call(fundAsset.getFundName()+"针对这个基金经理能力圈与路径依赖（即行业研究背景是否与现在管理的基金相匹配）进行分析（输出一段300字以内的实时准确直击重点的简洁描述）"));
        fundManagerAnalysis.setScaleAbilityAnalysis(bailianApiUtil.call(fundAsset.getFundName()+"针对这个基金经理规模驾驭能力（是否有管理大规模资金的成功经验）进行分析（输出一段300字以内的实时准确直击重点的简洁描述）"));
        fundManagerAnalysis.setProfessionalBackground(bailianApiUtil.call(fundAsset.getFundManager()+"针对这个基金经理的从业背景如学历、工作背景、获奖情况等进行分析（输出一段300字以内的实时准确直击重点的简洁描述）"));
        fundManagerAnalysis.setProductManagementAnalysis(bailianApiUtil.call(fundAsset.getFundName()+"针对这个基金经理的产品管理情况进行分析（看他是否“超负荷”工作。如果一个人名下管了十几只不同类型（如全市场选股、行业主题、量化对冲）的基金，精力难免被分散）（输出一段300字以内的实时准确直击重点的简洁描述）"));
        fundManagerAnalysis.setStabilityAnalysis(bailianApiUtil.call(fundAsset.getFundName()+"针对这个基金经理的稳定性情况【即如果他频繁跳槽，或者管理的基金频繁更换基金经理，这都是需要警惕的信号】进行分析（看这个基金经理是否能保持基金在某一个时间段内不跌停，或者不跌停的概率是多少）（输出一段300字以内的实时准确直击重点的简洁描述）"));
        fundManagerAnalysis.setPersonalHolding(bailianApiUtil.call(fundAsset.getFundName()+"针对这个基金经理的个人持有情况进行分析即【基金经理本人是否也持有了自己管理的基金？】（输出一段300字以内的实时准确直击重点的简洁描述）"));
        //System.out.println("获取到的基金经理分析数据为："+fundManagerAnalysis.toString());

        //8.基金基本数据入库
        fundAssetMapper.addFundAsset(fundAsset);

        //9.基金经理分析数据入库
        fundAssetMapper.addFundManagerAnalysis(fundManagerAnalysis);

        //10.关联台账拿到持仓情况
        //根据基金代码查询资产台账
        AssetLedger assetLedger = assetLedgerMapper.getAssetLedgerByCode(fundCode, AuthContextUtil.get().getId());
        if (assetLedger != null){
            FundHolding fundHolding = new FundHolding();
            fundHolding.setFundCode(fundCode);
            fundHolding.setCostAmount(assetLedger.getInvestAmount());
            fundHolding.setMarketValue(assetLedger.getAmount());
            fundHolding.setProfitLoss(fundHolding.getMarketValue().subtract(fundHolding.getCostAmount()));
            fundHolding.setProfitLossRate(fundHolding.getProfitLoss().divide(fundHolding.getCostAmount(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            fundHolding.setCreateBy("system");
            fundHolding.setOwner(assetLedger.getAssetOwner());

            //11.持仓情况入库
            fundAssetMapper.addFundHolding(fundHolding);
        }
        return "数据获取成功";
    }

    /**
     * 根据基金代码查询基金基本数据（用于资产台账穿透查看）
     * @param fundCode 基金代码
     * @return 基金基本数据
     */
    @Override
    public FundAsset getFundBaseDataByFundCode(String fundCode) {
        return fundAssetMapper.getFundBaseDataByFundCode(fundCode);
    }

    /**
     * 条件分页查询基金基本数据
     * @param current
     * @param limit
     * @param fundBaseDateDto
     * @return
     */
    @Override
    public PageInfo<FundAsset> getFundBaseDataByConditionAndPage(Integer current, Integer limit, FundBaseDateDto fundBaseDateDto) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //2.条件查询基金基本数据列表
        List<FundAsset> fundAssets = fundAssetMapper.getFundBaseDataByCondition(fundBaseDateDto);

        //3.创建分页对象
        PageInfo<FundAsset> fundAssetPageInfo = new PageInfo<>(fundAssets);

        //4.返回分页对象
        return fundAssetPageInfo;
    }

    /**
     * 条件分页查询基金净值数据
     * @param current
     * @param limit
     * @param fundComm
     * @return
     */
    @Override
    public PageInfo<FundNav> getFundNavByConditionAndPage(Integer current, Integer limit, FundComm fundComm) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //2.条件查询基金净值数据列表
        List<FundNav> fundNavigations = fundAssetMapper.getFundNavByCondition(fundComm);

        //3.创建分页对象
        PageInfo<FundNav> fundNavPageInfo = new PageInfo<>(fundNavigations);

        //4.返回分页对象
        return fundNavPageInfo;
    }

    /**
     * 根据基金代码获取基金经理分析数据
     * @param fundCode
     * @return
     */
    @Override
    public FundManagerAnalysis getFundManagerAnalysisByCode(String fundCode) {
        return fundAssetMapper.getFundManagerAnalysisByCode(fundCode);
    }

    /**
     * 根据基金代码获取基金持仓数据
     * @param fundCode
     * @return
     */
    @Override
    public FundHolding getFundHoldingByCode(String fundCode) {
        return fundAssetMapper.getFundHoldingByCode(fundCode, AuthContextUtil.get().getId());
    }

    /**
     * 条件分页查询基金交易与流水数据
     * @param current
     * @param limit
     * @param fundComm
     * @return
     */
    @Override
    public PageInfo<FundTransaction> getFundTransactionByConditionAndPage(Integer current, Integer limit, FundComm fundComm) {
        fundComm.setOwner(AuthContextUtil.get().getId());
        //1.开启分页
        PageHelper.startPage(current, limit);

        //2.条件查询基金交易与流水数据列表
        List<FundTransaction> fundTransactions = fundAssetMapper.getFundTransactionByCondition(fundComm);

        //3.创建分页对象
        PageInfo<FundTransaction> fundTransactionPageInfo = new PageInfo<>(fundTransactions);

        return fundTransactionPageInfo;
    }

    /**
     * 条件分页查询查询基金分红数据
     * @param current
     * @param limit
     * @param fundComm
     * @return
     */
    @Override
    public PageInfo<FundDividend> getFundDividendByConditionAndPage(Integer current, Integer limit, FundComm fundComm) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //2.条件查询基金分红数据列表
        List<FundDividend> fundDividends = fundAssetMapper.getFundDividendByCondition(fundComm);

        //3.创建分页对象
        PageInfo<FundDividend> fundDividendPageInfo = new PageInfo<>(fundDividends);

        return fundDividendPageInfo;
    }

    /**
     * 根据基金代码获取基金风险收益数据
     * @param fundCode
     * @return
     */
    @Override
    public List<FundRiskPerformance> getFundRiskPerformance(String fundCode) {
        return fundAssetMapper.getFundRiskPerformance(fundCode);
    }

    /**
     * 条件分页获取基金持仓信息
     * @param current
     * @param limit
     * @param fundComm
     * @return
     */
    @Override
    public PageInfo<FundPortfolio> getFundPortfolioByConditionAndPage(Integer current, Integer limit, FundComm fundComm) {
        //1.开启分页
        PageHelper.startPage(current, limit);

        //2.条件查询基金持仓数据列表
        List<FundPortfolio> fundPortfolios = fundAssetMapper.getFundPortfolioByCondition(fundComm);

        //3.创建分页对象
        PageInfo<FundPortfolio> fundPortfolioPageInfo = new PageInfo<>(fundPortfolios);

        return fundPortfolioPageInfo;
    }

    /**
     * 更新基金资产信息
     * @param fundAsset
     */
    @Override
    public void updateFundBaseAsset(FundAsset fundAsset) {
        fundAsset.setUpdateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.updateFundBaseAsset(fundAsset);
    }

    /**
     * 更新基金经理分析信息
     * @param fundManagerAnalysis
     */
    @Override
    public void updateFundManagerAnalysis(FundManagerAnalysis fundManagerAnalysis) {
        fundManagerAnalysis.setUpdateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.updateFundManagerAnalysis(fundManagerAnalysis);
    }

    /**
     * 更新基金持仓信息
     * @param fundHolding
     */
    @Override
    public void updateFundHolding(FundHolding fundHolding) {
        if(fundHolding.getId() == null){
            fundHolding.setCreateBy(AuthContextUtil.get().getUserName());
            fundHolding.setOwner(AuthContextUtil.get().getId());

            fundAssetMapper.addFundHolding(fundHolding);
        }else {
            fundHolding.setUpdateBy(AuthContextUtil.get().getUserName());
            fundAssetMapper.updateFundHolding(fundHolding);
        }
    }

    /**
     * 添加基金净值数据
     * @param fundNav
     */
    @Override
    public void addFundNav(FundNav fundNav) {
        fundAssetMapper.addFundNav(fundNav);
    }

    /**
     * 更新基金净值数据
     * @param fundNav
     */
    @Override
    public void updateFundNav(FundNav fundNav) {
        fundAssetMapper.updateFundNav(fundNav);
    }

    /**
     * 根据id删除基金净值数据
     * @param id
     */
    @Override
    public void deleteFundNav(Long id) {
        fundAssetMapper.deleteFundNav(id);
    }

    /**
     * 添加基金交易数据
     * @param fundTransaction
     */
    @Override
    public void addFundTransaction(FundTransaction fundTransaction) {
        fundTransaction.setCreateBy(AuthContextUtil.get().getUserName());
        fundTransaction.setOwner(AuthContextUtil.get().getId());
        fundAssetMapper.addFundTransaction(fundTransaction);
    }

    /**
     * 更新基金交易数据
     * @param fundTransaction
     */
    @Override
    public void updateFundTransaction(FundTransaction fundTransaction) {
        fundTransaction.setUpdateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.updateFundTransaction(fundTransaction);
    }

    /**
     * 根据id删除基金交易数据
     * @param id
     */
    @Override
    public void deleteFundTransaction(Long id) {
        fundAssetMapper.deleteFundTransaction(id);
    }

    /**
     * 添加基金分红数据
     * @param fundDividend
     */
    @Override
    public void addFundDividend(FundDividend fundDividend) {
        fundDividend.setCreateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.addFundDividend(fundDividend);
    }

    /**
     * 更新基金分红数据
     * @param fundDividend
     */
    @Override
    public void updateFundDividend(FundDividend fundDividend) {
        fundDividend.setUpdateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.updateFundDividend(fundDividend);
    }

    /**
     * 根据id删除基金分红数据
     * @param id
     */
    @Override
    public void deleteFundDividend(Long id) {
        fundAssetMapper.deleteFundDividend(id);
    }

    /**
     * 添加基金风险绩效数据
     * @param fundRiskPerformance
     */
    @Override
    public void addFundRiskPerformance(FundRiskPerformance fundRiskPerformance) {
        fundRiskPerformance.setCreateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.addFundRiskPerformance(fundRiskPerformance);
    }

    /**
     * 更新基金风险绩效数据
     * @param fundRiskPerformance
     */
    @Override
    public void updateFundRiskPerformance(FundRiskPerformance fundRiskPerformance) {
        fundRiskPerformance.setUpdateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.updateFundRiskPerformance(fundRiskPerformance);
    }

    /**
     * 根据id删除基金风险绩效数据
     * @param id
     */
    @Override
    public void deleteFundRiskPerformance(Long id) {
        fundAssetMapper.deleteFundRiskPerformance(id);
    }

    /**
     * 添加基金持仓信息数据
     * @param fundPortfolio
     */
    @Override
    public void addFundPortfolio(FundPortfolio fundPortfolio) {
        fundPortfolio.setCreateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.addFundPortfolio(fundPortfolio);
    }

    /**
     * 更新基金持仓信息数据
     * @param fundPortfolio
     */
    @Override
    public void updateFundPortfolio(FundPortfolio fundPortfolio) {
        fundPortfolio.setUpdateBy(AuthContextUtil.get().getUserName());
        fundAssetMapper.updateFundPortfolio(fundPortfolio);
    }

    /**
     * 根据id删除基金持仓信息数据
     * @param id
     */
    @Override
    public void deleteFundPortfolio(Long id) {
        fundAssetMapper.deleteFundPortfolio(id);
    }

    /**
     * 获取业绩走势echarts数据
     * @param fundComm
     * @return
     */
    @Override
    public List<NavEChartsVo> getNaveChartsByCondition(FundComm fundComm) {
        return fundAssetMapper.getNaveChartsByCondition(fundComm);
    }

    /**
     * 根据基金代码删除基金的全部数据
     * @param fund_code
     */
    @Override
    @Transactional
    public void deleteFundDataByCode(String fund_code) {
        //1.根据基金代码删除所有基金持仓数据
        fundAssetMapper.deleteFundPortfolioByCode(fund_code);

        //2.根据基金代码删除所有风险与绩效指标信息
        fundAssetMapper.deleteFundRiskPerformanceByCode(fund_code);

        //3.根据基金代码删除所有分红信息
        fundAssetMapper.deleteFundDividendByCode(fund_code);

        //4.根据基金代码删除所有交易与流水信息
        fundAssetMapper.deleteFundTransactionByCode(fund_code);

        //5.根据基金代码删除所有份额与持仓信息
        fundAssetMapper.deleteFundHoldingByCode(fund_code);

        //6.根据基金代码删除基金经理分析数据
        fundAssetMapper.deleteFundManagerAnalysisByCode(fund_code);

        //7.根据基金代码删除净值与估值信息数据
        fundAssetMapper.deleteFundNavByCode(fund_code);

        //8.根据基金代码删除基金基本数据
        fundAssetMapper.deleteFundAssetByCode(fund_code);
    }

    /**
     * 根据基金代码批量删除基金的全部数据
     * @param fund_codes
     */
    @Override
    @Transactional
    public void deleteFundDataByCodes(List<String> fund_codes) {
        if (fund_codes == null || fund_codes.isEmpty()) {
            return;
        }
        //1.根据基金代码批量删除所有基金持仓数据
        fundAssetMapper.deleteFundPortfolioByCodes(fund_codes);

        //2.根据基金代码批量删除所有风险与绩效指标信息
        fundAssetMapper.deleteFundRiskPerformanceByCodes(fund_codes);

        //3.根据基金代码批量删除所有分红信息
        fundAssetMapper.deleteFundDividendByCodes(fund_codes);

        //4.根据基金代码批量删除所有交易与流水信息
        fundAssetMapper.deleteFundTransactionByCodes(fund_codes);

        //5.根据基金代码批量删除所有份额与持仓信息
        fundAssetMapper.deleteFundHoldingByCodes(fund_codes);

        //6.根据基金代码批量删除基金经理分析数据
        fundAssetMapper.deleteFundManagerAnalysisByCodes(fund_codes);

        //7.根据基金代码批量删除净值与估值信息数据
        fundAssetMapper.deleteFundNavByCodes(fund_codes);

        //8.根据基金代码批量删除基金基本数据
        fundAssetMapper.deleteFundAssetByCodes(fund_codes);
    }

    /**
     * 获取基金重要数据 -> 基金持仓数据插入数据库，基金净值数据插入数据库
     * @param fundCode
     */
    @Override
    @Transactional
    public void addFundImportData(String fundCode) {
        //1.获取基金数据接口URL配置
        String fund_base_url = sysDictMapper.getConfigValueById("get_fund_base_data_url");
        String fund_url = fund_base_url.replace("{fund_code}", fundCode);
        String fund_content = fetchFundData(fund_url);

        if (fund_content == null) {
            throw new RuntimeException("获取基金数据失败，fundCode: " + fundCode);
        }

        //2.解析完整基金数据
        FundDataParser.FundData fundData = FundDataParser.parseAllFundData(fund_content);

        if (fundData.getFundCode() == null) {
            throw new RuntimeException("无" + fundCode + "这个代码的基金！");
        }

        //3.批量插入基金净值数据（增量插入，避免重复）
        List<FundDataParser.NavData> mergedNavList = fundData.getMergedWorthTrend();
        if (mergedNavList != null && !mergedNavList.isEmpty()) {
            // 查询数据库中该基金最新的净值日期
            Date latestNavDateInDb = fundAssetMapper.getLatestNavDate(fundCode);

            List<FundNav> navList = new ArrayList<>();
            for (FundDataParser.NavData navData : mergedNavList) {
                // 只插入数据库最新净值日期之后的数据
                if (latestNavDateInDb != null && !navData.getDate().after(latestNavDateInDb)) {
                    continue;
                }
                FundNav fundNav = new FundNav();
                fundNav.setFundCode(fundCode);
                fundNav.setNavDate(navData.getDate());
                fundNav.setUnitNav(navData.getNav());
                fundNav.setAccumulatedNav(navData.getAccumulatedNav());
                fundNav.setDailyChangeRate(navData.getDailyReturn());
                // valuation为空，createTime由数据库now()自动设置
                navList.add(fundNav);
            }

            if (!navList.isEmpty()) {
                fundAssetMapper.batchAddFundNav(navList);
                logger.info("基金{}新增净值数据{}条", fundCode, navList.size());
            } else {
                logger.info("基金{}净值数据已是最新，无需插入", fundCode);
            }
        }

        //4.批量插入基金持仓信息数据（前十大重仓股）- 已有数据则跳过
        List<FundDataParser.StockInfo> stockInfoList = fundData.getStockInfoList();
        if (stockInfoList != null && !stockInfoList.isEmpty()) {
            // 检查数据库中是否已有该基金的持仓数据
            int portfolioCount = fundAssetMapper.isPortfolioExistByCode(fundCode);
            if (portfolioCount > 0) {
                logger.info("基金{}持仓数据已存在，跳过插入", fundCode);
            } else {
                List<FundPortfolio> portfolioList = new ArrayList<>();
                // 获取最新持仓日期（用净值数据的最新日期作为持仓日期）
                Date portfolioDate = null;
                if (mergedNavList != null && !mergedNavList.isEmpty()) {
                    portfolioDate = mergedNavList.get(mergedNavList.size() - 1).getDate();
                }

                for (FundDataParser.StockInfo stockInfo : stockInfoList) {
                    FundPortfolio fundPortfolio = new FundPortfolio();
                    fundPortfolio.setFundCode(fundCode);
                    fundPortfolio.setPortfolioDate(portfolioDate);
                    fundPortfolio.setPositionType(1); // 1-股票
                    fundPortfolio.setPositionCode(stockInfo.getRealCode());
                    fundPortfolio.setPositionName(stockInfo.getName());
                    // positionQuantity、positionMarketValue、netRatio、industryType为空
                    fundPortfolio.setDataSource(2); // 2-天天基金
                    fundPortfolio.setCreateBy("system");
                    // createTime由数据库now()自动设置，updateTime、updateBy为空
                    portfolioList.add(fundPortfolio);
                }
                fundAssetMapper.batchAddFundPortfolio(portfolioList);
                logger.info("基金{}新增持仓数据{}条", fundCode, portfolioList.size());
            }
        }

        //5.更新基金可变数据（收益率、资产配置、规模、持有人结构等）
        if (fundAssetMapper.isExistByCode(fundCode) > 0) {
            FundAsset updateAsset = new FundAsset();
            updateAsset.setFundCode(fundCode);
            updateAsset.setUpdateBy("system");

            // 收益率
            if (fundData.getReturn1Month() != null) {
                updateAsset.setReturn1m(BigDecimal.valueOf(fundData.getReturn1Month()));
            }
            if (fundData.getReturn3Month() != null) {
                updateAsset.setReturn3m(BigDecimal.valueOf(fundData.getReturn3Month()));
            }
            if (fundData.getReturn6Month() != null) {
                updateAsset.setReturn6m(BigDecimal.valueOf(fundData.getReturn6Month()));
            }
            if (fundData.getReturn1Year() != null) {
                updateAsset.setReturn1y(BigDecimal.valueOf(fundData.getReturn1Year()));
            }

            // 资产配置（取最新一期数据，即列表最后一个元素）
            FundDataParser.AssetAllocation assetAllocation = fundData.getAssetAllocation();
            if (assetAllocation != null) {
                if (assetAllocation.getNetAssets() != null && !assetAllocation.getNetAssets().isEmpty()) {
                    Double latestNetAssets = assetAllocation.getNetAssets().get(assetAllocation.getNetAssets().size() - 1);
                    updateAsset.setNetAssets(String.valueOf(latestNetAssets));
                }
                if (assetAllocation.getStockRatio() != null && !assetAllocation.getStockRatio().isEmpty()) {
                    updateAsset.setStockRatio(BigDecimal.valueOf(assetAllocation.getStockRatio().get(assetAllocation.getStockRatio().size() - 1)));
                }
                if (assetAllocation.getBondRatio() != null && !assetAllocation.getBondRatio().isEmpty()) {
                    updateAsset.setBondRatio(BigDecimal.valueOf(assetAllocation.getBondRatio().get(assetAllocation.getBondRatio().size() - 1)));
                }
                if (assetAllocation.getCashRatio() != null && !assetAllocation.getCashRatio().isEmpty()) {
                    updateAsset.setCashRatio(BigDecimal.valueOf(assetAllocation.getCashRatio().get(assetAllocation.getCashRatio().size() - 1)));
                }
            }

            // 规模变动（取最新一期规模）
            FundDataParser.FluctuationScale fluctuationScale = fundData.getFluctuationScale();
            if (fluctuationScale != null && fluctuationScale.getScale() != null && !fluctuationScale.getScale().isEmpty()) {
                updateAsset.setLatestScale(BigDecimal.valueOf(fluctuationScale.getScale().get(fluctuationScale.getScale().size() - 1)));
            }

            // 持有人结构（取最新一期数据）
            FundDataParser.HolderStructure holderStructure = fundData.getHolderStructure();
            if (holderStructure != null) {
                if (holderStructure.getInstitutionRatio() != null && !holderStructure.getInstitutionRatio().isEmpty()) {
                    updateAsset.setInstitutionRatio(BigDecimal.valueOf(holderStructure.getInstitutionRatio().get(holderStructure.getInstitutionRatio().size() - 1)));
                }
                if (holderStructure.getIndividualRatio() != null && !holderStructure.getIndividualRatio().isEmpty()) {
                    updateAsset.setIndividualRatio(BigDecimal.valueOf(holderStructure.getIndividualRatio().get(holderStructure.getIndividualRatio().size() - 1)));
                }
                if (holderStructure.getInternalRatio() != null && !holderStructure.getInternalRatio().isEmpty()) {
                    updateAsset.setInternalRatio(BigDecimal.valueOf(holderStructure.getInternalRatio().get(holderStructure.getInternalRatio().size() - 1)));
                }
            }

            fundAssetMapper.updateFundDynamicDataByCode(updateAsset);
            logger.info("基金{}可变数据更新成功", fundCode);
        } else {
            logger.info("基金{}在t_fund_asset中不存在，跳过可变数据更新", fundCode);
        }

        //6.更新基金经理可变数据（星级、从业时间、管理规模、能力评分等）
        FundManagerAnalysis existingManager = fundAssetMapper.getFundManagerAnalysisByCode(fundCode);
        if (existingManager != null) {
            List<FundDataParser.FundManager> fundManagers = fundData.getFundManagers();
            FundDataParser.PerformanceEvaluation performanceEvaluation = fundData.getPerformanceEvaluation();

            FundManagerAnalysis updateManager = new FundManagerAnalysis();
            updateManager.setFundCode(fundCode);
            updateManager.setUpdateBy("system");

            // 基金经理基础信息（取第一个经理，即当前在任经理）
            if (fundManagers != null && !fundManagers.isEmpty()) {
                FundDataParser.FundManager currentManager = fundManagers.get(0);
                updateManager.setManagerName(currentManager.getName());
                updateManager.setStarRating(BigDecimal.valueOf(currentManager.getStar()));
                // 从业时间转换为数值（如 "12.3年" → 12.3）
                String workTimeStr = FundDataParser.convertWorkTimeToYears(currentManager.getWorkTime());
                try {
                    updateManager.setWorkTime(new BigDecimal(workTimeStr.replace("年", "")));
                } catch (NumberFormatException e) {
                    logger.warn("基金经理从业时间解析失败：{}", workTimeStr);
                }
                // 管理规模（如 "113.11亿(4只基金)"，去除非数字字符后解析）
                if (currentManager.getFundSize() != null && !currentManager.getFundSize().isEmpty()) {
                    String fundSizeStr = currentManager.getFundSize().replaceAll("[^0-9.-]", "");
                    if (!fundSizeStr.isEmpty()) {
                        try {
                            updateManager.setManageScale(new BigDecimal(fundSizeStr));
                        } catch (NumberFormatException e) {
                            logger.warn("基金经理管理规模解析失败：{}", currentManager.getFundSize());
                        }
                    }
                }
            }

            // 业绩评价评分（avgScore为0且scores全为0或null时视为无效数据，不更新）
            if (performanceEvaluation != null) {
                List<Double> scores = performanceEvaluation.getScores();
                boolean hasValidScore = performanceEvaluation.getAvgScore() > 0
                        || (scores != null && scores.stream().anyMatch(s -> s != null && s > 0));
                if (hasValidScore) {
                    if (performanceEvaluation.getAvgScore() > 0) {
                        updateManager.setTotalScore(BigDecimal.valueOf(performanceEvaluation.getAvgScore()));
                    }
                    if (scores != null && scores.size() >= 5) {
                        // scores顺序：选证能力、收益率、抗风险、稳定性、择时能力
                        if (scores.get(0) != null && scores.get(0) > 0) {
                            updateManager.setStockSelectScore(BigDecimal.valueOf(scores.get(0)));
                        }
                        if (scores.get(1) != null && scores.get(1) > 0) {
                            updateManager.setReturnScore(BigDecimal.valueOf(scores.get(1)));
                        }
                        if (scores.get(2) != null && scores.get(2) > 0) {
                            updateManager.setRiskControlScore(BigDecimal.valueOf(scores.get(2)));
                        }
                        if (scores.get(3) != null && scores.get(3) > 0) {
                            updateManager.setStabilityScore(BigDecimal.valueOf(scores.get(3)));
                        }
                        if (scores.get(4) != null && scores.get(4) > 0) {
                            updateManager.setTimingScore(BigDecimal.valueOf(scores.get(4)));
                        }
                    }
                }
            }

            fundAssetMapper.updateFundManagerDynamicDataByCode(updateManager);
            logger.info("基金{}基金经理可变数据更新成功", fundCode);
        } else {
            logger.info("基金{}在t_fund_manager_analysis中不存在，跳过基金经理可变数据更新", fundCode);
        }

        //7.更新持仓信息数据
        //7.1获取基金台账数据
        AssetLedger assetLedger = assetLedgerMapper.getAssetLedgerByCode(fundCode, AuthContextUtil.get().getId());
        if (assetLedger != null){
            FundHolding fundHolding = new FundHolding();
            fundHolding.setFundCode(fundCode);
            fundHolding.setCostAmount(assetLedger.getInvestAmount());
            fundHolding.setMarketValue(assetLedger.getAmount());
            fundHolding.setProfitLoss(fundHolding.getMarketValue().subtract(fundHolding.getCostAmount()));
            fundHolding.setProfitLossRate(fundHolding.getProfitLoss().divide(fundHolding.getCostAmount(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            fundHolding.setUpdateBy(AuthContextUtil.get().getUserName());

            //11.更新持仓情况
            fundAssetMapper.updateFundHoldingByCode(fundHolding);
        }
    }

    /**
     * 获取业绩分析数据
     * @param fundComm
     * @return
     */
    @Override
    public PerformanceAnalysisVo getPerformanceAnalysisByCondition(FundComm fundComm) {
        fundComm.setOwner(AuthContextUtil.get().getId());
        return fundAssetMapper.getPerformanceAnalysisByCondition(fundComm);
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