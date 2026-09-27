---
name: stock-data-fetch-best-practice
description: 本项目股票数据获取的强制规范与最佳实践。当用户新增接口取数或爬虫功能、优化数据获取可靠性、要求URL配置入库、添加数据源兜底、涉及熔断降级或东财腾讯新浪字段口径时必须引用。不适用于非数据获取类需求。
---

# 股票数据获取与URL配置入库最佳实践

本项目（srhwzzqdn）数据获取经过多轮事故沉淀出的强制规范。做任何取数相关改动前先通读本文件，字段口径查 [references/data-sources.md](references/data-sources.md)。

## 铁律（用户明令的合规模型，违反即返工）

1. **全程单次尝试、零重试**：httpGet 单次（5s连接/8s读超时）；HTTP 客户端必须 `HttpClientBuilder.create().disableAutomaticRetries().build()`——`HttpClients.createDefault()` 会静默内部重试 NoHttpResponseException 3 次，放大封禁并饱和线程池。
2. **固定 UA/Referer，不轮换**：单一 FIXED_UA；Referer 按域固定——sina 必须 `https://finance.sina.com.cn/`（API 硬要求，其他值 403），其余用 `https://quote.eastmoney.com/`。
3. **无 race，接口优先串行**：东财→腾讯→新浪 依次兜底；东财在熔断期内 0 请求直接跳到下一源。race() 仅 MarketAnalysisServiceImpl 存量保留，勿扩散。
4. **频率控制 = 全局熔断器 + 200ms 批量间隔**：批量循环内 sleep 150~200ms，不设其他重试。

## URL 配置入库模式

- 配置表 `t_sys_comm_config`（id / value / fallback_url / status / description / update_by），代码用 `InterfaceConfigUtil.getUrl(key, yml默认值)` 读取（自带 5 分钟缓存，改库自动生效）。
- **入库前必须先实测**：curl 确认 200 且字段语义正确（见 references 的口径表），再执行：
  ```sql
  insert into t_sys_comm_config (id, value, fallback_url, status, description, update_by)
  values ('stock_xxx_url', 'https://...', 'https://...', '1', '用途+口径说明（纯host还是含路径后缀）+验证日期', 'admin')
  on duplicate key update value = values(value), fallback_url = values(fallback_url), description = values(description);
  ```
- **口径必须写进 description**：纯 host（代码拼 `/q=` 前缀）与 host+完整路径（代码直接拼代码列表）是两种口径，**同一 host 的不同调用方必须用独立配置键**——共用一键导致 DB 值覆盖默认后另一调用方拼错 URL（stock_tx_quote_url vs stock_tx_quote_batch_url 的教训）。
- **例外**：AI endpoint（ai_deepseek_url）用户已明确不入库、代码不读，直接用 application-dev.yml `ai.common`。勿再加回读库逻辑。

## 全局熔断器（必须共享，绝不新建 per-service 熔断）

- 组定义（StockDataFetcher）：GROUP_KLINE 5min / GROUP_CLIST 3min / GROUP_QUOTE 3min / GROUP_FLOW 3min / GROUP_DATACENTER 5min / GROUP_POOL 3min。首个失败即整组熔断，期间所有同组请求 0 发出。
- API：`blocked(group)` 判断跳过、`markFail(group, ms)` 标记、`markSuccess(group)` 解除、`remainMs(group)` 查剩余。
- StockQuoteUtil 按 URL 子串映射组（groupOf），**映射前必须先判 `if (!url.contains("eastmoney.com")) return null;`**——腾讯 fqkline 含 "kline" 字样曾被误映射进 GROUP_KLINE，导致东财熔断期腾讯兜底被误跳过、兜底失效。
- 新增兜底循环内每轮检查 `blocked`，命中立即 break，绝不继续探测。

## 兜底链模式

- 结构：主源（东财）失败或返回空 → 兜底源（腾讯/新浪/emweb/datacenter），**仅补缺失字段、绝不覆盖已有值**。
- 激活条件：兜底仅在主源缺失时执行——正常路径 0 增量请求，这是效率红线。
- **每个失败路径必须打日志**（HTTP null / 空数组 / 解析异常分别记），杜绝静默 `return null`（排查时零线索的教训）。
- 结果缓存：市场分析正常 30min、熔断降级 5min；选股类结果 60s。
- 兜底补数只认目标日期当日数据（如 fflow daykline 精确匹配 tradeDate），绝不串历史行。

## 动手前先验证数据源

- 写代码前先用 curl 实测目标 URL（`curl.exe -o tmp -w "%{http_code}"`）确认可达性与字段含义；**push2 黑洞 ≠ 全东财封禁**：push2/push2delay 被 RST 时 push2his、datacenter-web、emweb、腾讯、新浪往往仍 200，替代源要分家族选。
- 取不到数据时先分诊：日志查 `UnknownHostException`（本地 DNS 断网）vs `NoHttpResponseException`/连接重置（东财黑洞）；再查 StockDataFetcher RACE_POOL≥24 线程是否饱和（静默失败的常见根因）。

## 环境坑速查

- 后端编译：`$env:JAVA_HOME='D:\softSourceZip\jdk17'`（默认 1.8 会报 invalid target release: 17）。
- mysql：客户端 `D:\softSourceZip\mysql\sourceUrl\bin\mysql.exe`，库 srhwzzqdn root/123456；免密用 `--defaults-extra-file=临时cnf`（用后即删，勿在命令行/脚本硬编码密码）；中文 SQL 写 UTF-8 文件后 `--default-character-set=utf8mb4 -e "source 文件"` 执行；短选项 -h 有问题用 --host。
- PowerShell 5.1：Write/Edit 写的 .ps1 是无 BOM UTF-8，含中文注释会被按 GBK 误解析报假语法错误，执行前转 UTF-8 with BOM；Shell 工具内联 -Command 会吞 `$变量`，一律 `-File` 跑脚本。

详细字段口径与已验证 URL 模板：[references/data-sources.md](references/data-sources.md)
