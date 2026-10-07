# 数据源字段口径与已验证 URL 模板

全部为 2026-09 实测验证。新用字段前仍须重新 curl 验证。

## 东财（Referer 固定 https://quote.eastmoney.com/）

### clist / ulist 字段语义（两族字段含义不同，勿混用）
| 字段 | clist（列表） | ulist（批量快照） |
|---|---|---|
| 量比 | f10 | f10 |
| 成交额 | f50（元） | f50（元） |
| 总市值 | f20（元，÷1e8） | f20（元，÷1e8） |
| 流通市值 | f21（元，÷1e8） | f21（元，÷1e8） |
| 主力净流入 | f62（元，÷1e8） | f62（元，÷1e8） |
| 主力净占比 | f184（%） | f184（%） |
| 行业 | f100 | — |
| 概念 | f103（逗号串，截前5个、≤200字符） | f103 |

- 绝对值字段（f20/f21/f62）必须 ÷1e8，单位错误曾造成两次静默全失败。
- clist 单页硬上限 pz=100（>100 静默返回100），翻页 pn=1..N 间隔 200ms。
- 短线排序用 fid=f184（净占比），勿用 f62（被权重股垄断）。
- ulist 批量按 secid，60/批；f47/f48 内外盘返回不可靠负值，禁止采集。
- host 族：push2.eastmoney.com（主）/ push2delay（镜像）；ulist.np/get。

### 资金流日K（push2his 家族，push2 黑洞时常存活）
`https://push2his.eastmoney.com/api/qt/stock/fflow/daykline/get?lmt=2&klt=101&fields1=f1,f2,f3,f7&fields2=f51,f52,f57&secid={mkt}.{code}`
klines 行 = `日期,主力净流入(元),主力净占比(%)` → f[1]/1e8=亿、f[2]=净占比。只认当日行。

### K线（push2his）
`{host}/api/qt/stock/kline/get?secid={mkt}.{code}&klt=101&fqt=1&lmt={n}&end=20500101&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58`
fields2 顺序：日期/开/收/高/低/量/额。

### push2 get 单票报价可顺带的基本面字段（同 URL 0 增量请求，2026-09-27 验证）
f9=市盈率动态(×100) f23=市净率(×100) f100=行业名 f116=总市值(元,÷1e8) f117=流通市值(元,÷1e8)。

### push2his fflow daykline（资金流兜底，GROUP_FLOW 与 push2 主源共享熔断）
`https://push2his.eastmoney.com/api/qt/stock/fflow/daykline/get?lmt={n}&klt=101&fields1=f1,f2,f3,f7&fields2=f51,f52,f53,f54,f55&secid={mkt}.{code}`
klines 行 = `日期,主力净流入元,小单,中单,大单` 与 push2 主源格式一致。注意三个口径不同的配置键：util_flow_url（push2 主源）/ util_fflow_day_url（本模板 f51-f55 五列）/ stock_fflow_day_url（短线选股 f51,f52,f57 三列含净占比）。

### 技术指标模式（AI 输入，0 增量请求）
K线已拉到时纯 Java 计算 MA5/10/20、MACD(12,26,9)、KDJ(9,3,3) 直接注入文本，不要为算指标再发请求；bars<周期数时标注"样本不足"而非省略段落。

### datacenter-web（数据中心报表，带 GROUP_DATACENTER 熔断 5min）
- 业绩报表：`https://datacenter-web.eastmoney.com/api/data/v1/get?reportName=RPT_LICO_FN_CPD&...`（ROE 降序，价值选股兜底主源）
- 估值：`reportName=RPT_VALUEANALYSIS_DET`，sortColumns=SECURITY_CODE&sortTypes=1&pageSize=500 分页（≤15页）。字段 PE_TTM/PB_MRQ/TOTAL_MARKET_CAP/CLOSE_PRICE，无年初涨幅列；PE(TTM) 实时修正 = PE_TTM×(现价/截面收盘价)，年初涨幅 = (现价/上年末12-31 CLOSE_PRICE −1)×100
- 行业：`reportName=RPT_F10_BASIC_ORGINFO`，EM2016="一-二-三" 取第2段 ≈ clist f100；无 filter 按 SECURITY_CODE 排序 pageSize=500 可拉全市场
- 探测日期存在性：1 请求/天，body==null 立即放弃

### emweb（F10，push2 黑洞时的概念替代源）
`https://emweb.securities.eastmoney.com/PC_HSF10/CoreConception/PageAjax?code={SH|SZ|BJ}{code}`
ssbk 数组中 `IS_PRECISE='1'` 的 BOARD_NAME = 核心概念 ≈ f103。

### push2ex 涨停池（GROUP_POOL 3min）
涨停/炸板/跌停池（ZT/ZB/DT），熔断期返回 count=-1/0 降级、0 请求。

## 腾讯（无 Referer 要求；资金流 API 已死，无替代源）

### qt.gtimg.cn 实时行情（GBK 编码，中文乱但数字字段 ASCII 安全）
`https://qt.gtimg.cn/q=sz000001,sh600000,...`（批量逗号分隔）
v_szXXXXXX 按 "~" 切分：[3]最新价 [4]昨收 [5]今开 [31]涨跌额 [32]涨跌幅% [33]高 [34]低 [35]价/量/额 [36]量(手) [37]成交额(万) [38]换手% [43]振幅% [44]流通市值(亿) [45]总市值(亿) [47]涨停价 [48]跌停价 [49]量比。
指数同构（sh000001 / sz399001 / sz399006）；成交额兜底 = [37]万/1e4。
资金流 `q=ff_`/`ff_s_` 返回 v_pv_none_match="1"，proxy.finance.qq.com hqzx getMoneyFlow 404 —— 主力净流入无腾讯兜底。

### fqkline 日K（ Referer 可无，web.ifzq.gtimg.cn 已 501 废弃禁用）
`https://ifzq.gtimg.cn/appstock/app/fqkline/get?param={sh|sz}{code},day,,,{n},qfq`
返回 data[code].qfqday（指数/部分票为 day）；行 = [date, open, close, high, low, volume, ...]，可能混入分红对象（type!=null 跳过）。口径 [1]=开 [2]=收 [3]=高 [4]=低 [5]=量。镜像：proxy.finance.qq.com/ifzqgtimg 同路径。

## 新浪（Referer 硬要求：仅 https://finance.sina.com.cn/ 验证 200，其余 403）

### hq.sinajs.cn 实时行情
`https://hq.sinajs.cn/list=sz000001,...` GBK。split(',')：[0]名称 [1]今开 [2]昨收 [3]最新价 [8]量 [9]额

### hqnode 分页行情（短线候选兜底，具体 URL 以 ShortTermPickServiceImpl.fetchClistCandidatesFromSina 现有实现为准）
**必须按成交额（amount）desc 排序**：按换手率排再过滤流通市值 20-500亿 只剩 ~5 只（近零），按成交额排剩 ~76 只（2026-09-24 盘中实测）。

## 代码模板索引（改前先读，勿凭记忆重写）

- StockQuoteUtil：httpGet 单次尝试 + groupOf 熔断映射（先判 eastmoney.com）+ 东财→腾讯兜底链；util_quote_url / util_kline_url / util_flow_url / util_clist_url / util_topic_pool_url / util_tx_quote_url（纯host口径）/ util_tx_kline_url（完整路径口径）
- StockDataFetcher：熔断组 + executor + race（仅 MarketAnalysis 用）
- StockAssetServiceImpl：fetchStockBasic（东财→腾讯，双失败 markFail GROUP_QUOTE）、fetchKlineData（东财熔断跳过→tx→sina）、getFundamentalStocks（clist 熔断→datacenter 业绩报表兜底 + 腾讯行情/估值报表补齐）
- ShortTermPickServiceImpl：fetchClistCandidates（东财3页并行→sina hqnode 按成交额）、enrichPoolSnapshot（ulist→腾讯批量→新浪批量，60/批）、fetchKlines（16线程池内 east→tx→sina 串行）、backfillMainInflowFromFflow（fflow 日K 补主力净流入）、fillIndustryFromDb（t_stock_basic 补行业）
- InterfaceConfigUtil：getUrl(key, 默认值) 5 分钟缓存
