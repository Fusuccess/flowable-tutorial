# Flowable 教程（二）：流程部署（Process Deployment）
流程部署是将流程定义文件导入 Flowable 引擎的过程。只有部署成功后，流程定义才会被 Flowable 识别，允许启动流程实例。


--- 

## 当前目录结构说明
```tree
tutorial02_deployment/
├── DeploymentController.java       # 流程部署的 HTTP 接口，支持通过浏览器或 Postman 触发部署
├── DeploymentService.java          # 部署核心逻辑，包括从 classpath 和数据库部署的方法
├── flowable_process.sql            # 模拟数据库部署场景所需的建表脚本（保存流程 XML）
└── README.md                       # 教程文档，包含部署方式、代码示例、常见问题等说明
```

--- 

## 部署方式
#### 流程部署方式一：自动扫描部署（Classpath 部署）</br>
application.properties 示例（Spring Boot 默认配置）：
```properties
# flowable配置，默认自动部署classpath下bpmn文件
flowable.check-process-definitions=true
```
#### 流程部署方式二：代码手动部署</br>
```java
@Autowired
private RepositoryService repositoryService;

public void deployProcess() {
    Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("bpmn/leave-approve.bpmn20.xml")
        .name("请假流程部署")
        .deploy();
}
```
#### 流程部署方式三：从数据库读取 XML 并部署</br>
```java
@Autowired
private RepositoryService repositoryService;

@Autowired
private JdbcTemplate jdbcTemplate;

public void deployProcessFromDb() {
    // 从数据库读取 BPMN XML 字符串
    String sql = "SELECT process_xml FROM flowable_process WHERE process_key = ?";
    String processKey = "leaveProcess";

    String bpmnXml = jdbcTemplate.queryForObject(sql, new Object[]{processKey}, String.class);

    if (bpmnXml != null && !bpmnXml.isEmpty()) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));

        Deployment deployment = repositoryService.createDeployment()
            .addInputStream(processKey + ".bpmn20.xml", inputStream)
            .name("数据库流程部署：" + processKey)
            .deploy();

        System.out.println("数据库流程部署成功，部署ID：" + deployment.getId());
    } else {
        System.out.println("未找到流程定义 XML");
    }
}
```
---

## 流程定义表结构

部署后的流程定义存储在如下数据库表中：

| 表名                  | 说明              |
| ------------------- | --------------- |
| ACT\_RE\_DEPLOYMENT | 部署记录            |
| ACT\_RE\_PROCDEF    | 流程定义信息          |
| ACT\_GE\_BYTEARRAY  | 流程定义的 XML 二进制内容 |

---

| 编号  | 问题描述                             | 原因分析                                                          | 解决方案                                                                                                       |
| --- | -------------------------------- | ------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Q1  | 启动项目时未自动部署流程定义文件                 | Flowable 默认只扫描 classpath:/processes/ 和 /bpmn/ 目录，或者文件名后缀不符合规则 | 确保流程文件放在 src/main/resources/bpmn/，并以 .bpmn20.xml 结尾，或通过 flowable.process-definition-location-prefix 修改默认路径 |
| Q2  | 项目启动后控制台无任何部署日志                  | 日志级别过低或流程文件未被扫描到                                              | 开启日志输出：logging.level.org.flowable=INFO，并确保流程文件路径正确                                                         |
| Q3  | 手动部署成功但数据库中无 ACT\_RE\_PROCDEF 记录 | RepositoryService 部署时未调用 .deploy() 方法                         | 部署代码中必须调用 .deploy()，否则不会真正注册流程定义                                                                           |
| Q4  | 部署时报 XML 解析错误                    | 流程 XML 文件语法错误或命名空间不完整                                         | 使用 Flowable Modeler 或在线工具校验 XML 格式，确保根标签为 \<bpmn\:definitions ...> 且命名空间完整                                 |
| Q5  | 从数据库读取 XML 部署时报错                 | 数据库字段为空或读取异常                                                  | 确保数据库字段 process\_xml 非空，打印日志确认内容已成功读取                                                                      |
| Q6  | 多次部署同一个流程但版本未更新                  | 流程内容无变更，Flowable 不生成新版本                                       | 修改流程内容或部署文件名，使其在逻辑上为新版本                                                                                    |
| Q7  | 自动部署的流程部署名为空                     | Spring Boot 自动部署不会指定 name 属性                                  | 如需自定义部署名，建议使用 RepositoryService 手动部署并调用 .name("xxx")                                                       |
| Q8  | 数据库中未生成流程定义相关表                   | 没有配置 flowable.database-schema-update=true，或数据库连接错误            | 检查配置项 flowable.database-schema-update，确保数据源配置正确且数据库存在                                                      |
| Q9  | 中文流程名显示乱码                        | 数据库或 JDBC URL 编码设置不正确                                         | 在 spring.datasource.url 中添加 characterEncoding=utf8，确保数据库使用 utf8mb4 字符集                                     |
| Q10 | 启动流程时报“找不到流程定义”                  | 流程 key 错误或部署未成功                                               | 使用 repositoryService.createProcessDefinitionQuery().list() 查询部署列表确认流程是否存在及其 key                            |

--- 
## 小结

| 部署方式          | 优缺点            | 适用场景           |
| ------------- | -------------- | -------------- |
| 自动扫描Classpath | 简单方便，无法动态更新    | 流程文件固定不常变      |
| 代码手动部署        | 灵活，支持运行时动态部署   | 流程上传、版本管理      |
| 数据库读取部署       | 动态化、支持热更新、集中管理 | 需要流程管理后台和流程模型器 |
