# 基于 SpringBoot、redis、mybatis-plus、rabbitMQ、langchain4j 的智能数据分析平台。

### 区别于传统 BI，用户只需要

### 导入原始数据集、并输入分析诉求，就能自动生成可视化图表及分析结论，实现数据分析的降本增效
技术栈：SpringBoot、redis、mybatis-plus、rabbitMQ、langchain4j
**主要工作**

1. 业务流程：后端自定义 Prompt 预设模板并封装用户输入的数据和分析诉求，通过对接 

AIGC 接口生成可视化图表 json 配置和分析结论，返回给前端渲染。

2. 由于 AIGC 的输入 Token 限制，使用 Easy Excel 解析用户上传的 XLSX 表格数据文件并压缩

为 CSV，实测提高了 20% 的单次输入数据量、并节约了成本。

3. 为保证系统的安全性，对用户上传的原始数据文件进行了后缀名、大小、内容等多重校验

4. 为防止某用户恶意占用系统资源，基于 Redisson 的 RateLimiter 实现分布式限流，控制单用

户访问的频率。

5. 由于 AIGC 的响应时间较长，基于自定义 IO 密集型线程池+ 任务队列实现了 AIGC 的并发执行和异步化，提交任务后即可响应前端，提高用户体验。

6. 由于本地任务队列重启丢失数据，使用 RabbitMQ（分布式消息队列）来接受并持久化任务消

息，通过 Direct 交换机转发给解耦的 AI 生成模块消费并处理任务，提高了系统的可靠性。
