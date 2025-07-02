# Flowable 教程（三）：流程实例、流程变量、任务处理

流程实例（Process Instance）是流程定义的运行中实体。部署的流程定义就像“模板”，而每次启动流程就相当于创建了该模板的一个具体实例。

一个流程定义可以被多次启动，生成多个实例

每个实例之间数据互不干扰

流程实例运行中的状态保存在 ACT_RU_ 表中

运行结束后，数据进入 ACT_HI_ 表（历史）


>本教程将引导你通过几个简单的 API 接口，使用 Flowable 引擎进行流程的启动、查询、任务办理和历史查看。  本教程假设你已经配置好 Flowable 引擎，并部署了一个名为 "leaveApproval" 的请假审批流程定义。

**先决条件:**

*   Flowable 引擎已配置并运行。
*   已部署一个流程定义，其 process ID 为 "leaveApproval"。 ( `<bpmn:process id="leaveApproval">` )
*   流程定义中至少包含 `employee` 和 `manager` 两个 User Task 的 assignee，即请假人和审批人。

**准备工作**

你需要引入以下Flowable Service：

*   `RuntimeService`: 用于启动流程实例、管理流程变量等。
*   `TaskService`: 用于查询任务、认领任务、完成任务等。
*   `HistoryService`: 用于查询历史流程实例。

```java
@Autowired
private RuntimeService runtimeService;

@Autowired
private TaskService taskService;

@Autowired
private HistoryService historyService;
```

**一、启动流程实例**

*   **接口:** `/start` (POST 请求)
*   **参数:**
    *   `employee`:  请假人（对应流程定义中 `employee` 角色的用户）。
    *   `manager`:  审批人（对应流程定义中 `manager` 角色的用户）。
*   **功能:**  启动一个 "leaveApproval" 流程实例，并将 `employee` 和 `manager` 作为流程变量传递给对应的 User Task。
*   **说明:**
    *   `startProcessInstanceByKey("leaveApproval", vars)`：通过流程定义 Key 启动流程实例。  `leaveApproval` 是 BPMN 文件中 `<bpmn:process id="leaveApproval">` 定义的 ID。
    *   `vars`：是一个 Map，用于存储流程变量。  流程变量可以在流程的各个环节中使用，例如绑定 User Task 的 assignee。
    *   启动成功后，流程实例的信息会写入 Flowable 的 `ACT_RU_EXECUTION` 表中。

```java
/**
 * 启动流程实例
 */
@PostMapping("/start")
public List<String> start(String employee,String manager) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("employee", employee);
    vars.put("manager", manager);

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("leaveApproval", vars);
    System.out.println("流程实例ID: " + instance.getId());
    System.out.println("流程定义ID: " + instance.getProcessDefinitionId());
    List<String> instanceInfo = new ArrayList<>();
    instanceInfo.add("【流程实例ID:" + instance.getId() + "】 【流程定义ID: " + instance.getProcessDefinitionId()+"】");
    return instanceInfo;
}
```

**二、查询流程实例**

*   **接口:** `/instanceList` (POST 请求)
*   **参数:**
    *   `key`: 流程定义 Key (例如: "leaveApproval")
*   **功能:** 查询所有正在运行的，流程定义 Key 为指定值的流程实例。

```java
/**
 * 查询流程实例
 */
@PostMapping("/instanceList")
public List<String> instanceList(String key) {
    List<ProcessInstance> instances = runtimeService
            .createProcessInstanceQuery()
            .processDefinitionKey(key)
            .active()
            .list();

    List<String> instanceList = new ArrayList<>();
    for (ProcessInstance pi : instances) {
        instanceList.add("【运行中流程实例ID: " + pi.getId()+"】");
    }
    return instanceList;
}
```

**三、根据用户查询当前任务**

*   **接口:** `/taskListByUser` (POST 请求)
*   **参数:**
    *   `user`:  用户名 (assignee)。
*   **功能:** 查询指定用户待办的 "leaveApproval" 流程的任务列表。
*   **说明:**
    *   `taskAssignee(user)`：指定任务的 assignee 为 `user`。
    *   查询结果包含任务 ID、任务名称、当前节点定义 ID 和 assignee 信息。
    *   如果任务尚未分配 assignee，则 assignee 为 null，表示该任务可能为候选组任务。

```java
/**
 * 查询当前任务
 */
@PostMapping("/taskListByUser")
public List<String> taskListByUser(String user) {
    List<Task> tasks = taskService
            .createTaskQuery()
            .processDefinitionKey("leaveApproval")
            .taskAssignee(user)
            .list();

    List<String> taskList = new ArrayList<>();
    for (Task task : tasks) {
        String assignee = task.getAssignee();
        if (assignee != null) {
            taskList.add("【当前任务ID：" + task.getId()+ "】【" + "任务名称：" + task.getName()+"】 【当前节点定义ID：" + task.getTaskDefinitionKey()+"】【任务当前审批人（assignee）：" + assignee+"】");
        } else {
            taskList.add("【当前任务ID：" + task.getId()+ "】【" + "任务名称：" + task.getName()+"】 【当前节点定义ID：" + task.getTaskDefinitionKey()+"】【"+"该任务尚未分配具体审批人（可能为候选组）"+"】");
        }
    }
    return taskList;
}
```

**四、根据流程实例ID查询当前任务**

*   **接口:** `/taskListByInstanceId` (POST 请求)
*   **参数:**
    *   `instanceId`:  流程实例ID。
*   **功能:** 查询指定流程实例ID的任务列表。

```java
/**
 * 查询当前任务
 */
@PostMapping("/taskListByInstanceId")
public List<String> taskListByInstanceId(String instanceId) {
    List<Task> tasks = taskService
            .createTaskQuery()
            .processInstanceId(instanceId)
            .list();

    List<String> taskList = new ArrayList<>();
    for (Task task : tasks) {
        String assignee = task.getAssignee();
        if (assignee != null) {
            taskList.add("【当前任务ID：" + task.getId()+ "】【" + "任务名称：" + task.getName()+"】 【当前节点定义ID：" + task.getTaskDefinitionKey()+"】【任务当前审批人（assignee）：" + assignee+"】");
        } else {
            taskList.add("【当前任务ID：" + task.getId()+ "】【" + "任务名称：" + task.getName()+"】 【当前节点定义ID：" + task.getTaskDefinitionKey()+"】【"+"该任务尚未分配具体审批人（可能为候选组）"+"】");
        }
    }
    return taskList;
}
```

**五、完成任务**

*   **接口:** `/taskComplete` (POST 请求)
*   **参数:**
    *   `taskId`:  要完成的任务的 ID。
*   **功能:** 完成指定 ID 的任务，并使流程推进到下一个节点。
*   **说明:**
    *   `taskService.complete(taskId)`：完成任务。  任务完成后，流程会自动根据流程定义推进到下一个节点。
    *   如果下一个节点是 User Task，则需要切换用户并重新查询任务列表。

```java
/**
 * 完成指定任务
 */
@PostMapping("/taskComplete")
public void complete(String taskId) {
    taskService.complete(taskId);
}
```

**六、查看历史流程**

*   **接口:** `/historyList` (POST 请求)
*   **参数:**  无
*   **功能:** 查询所有已完成的流程实例，并按结束时间降序排列。
*   **说明:**
    *   `finished()`：只查询已完成的流程实例。
    *   `orderByProcessInstanceEndTime().desc()`：按流程实例结束时间降序排序。
    *   查询结果包含流程名称和流程耗时。

```java
/**
 * 查看历史流程
 */
@PostMapping("/historyList")
public List<String> historyList() {
    List<HistoricProcessInstance> finished = historyService
            .createHistoricProcessInstanceQuery()
            .finished().orderByProcessInstanceEndTime()
            .desc()
            .list();

    List<String> historyList = new ArrayList<>();
    for (HistoricProcessInstance h : finished) {
        historyList.add("【流程：" + h.getName() + "，耗时：" + h.getDurationInMillis() + "ms】");
    }
    return historyList;
}
```

**流程示例:**

1.  **启动流程:**  调用 `/start` 接口，传入 `employee` 为 "张三"，`manager` 为 "李四"。  流程启动后，会在 `ACT_RU_EXECUTION` 表中创建一个新的流程实例。
2.  **查询张三的任务:** 调用 `/taskListByUser` 接口，传入 `user` 为 "张三"。  会查到张三的 "提交请假申请" 任务。
3.  **完成张三的任务:** 调用 `/taskComplete` 接口，传入张三的任务 ID。  任务完成后，流程会自动流转到 "经理审批" 节点。
4.  **查询李四的任务:** 调用 `/taskListByUser` 接口，传入 `user` 为 "李四"。  会查到李四的 "经理审批" 任务。
5.  **完成李四的任务:** 调用 `/taskComplete` 接口，传入李四的任务 ID。  任务完成后，流程结束。
6.  **查看历史流程:** 调用 `/historyList` 接口，可以查看到已完成的请假流程，并显示流程耗时。

**注意事项:**

*   本教程仅提供了一个简单的流程示例。 在实际应用中，流程可能会更加复杂，需要根据具体的业务需求进行设计。
*   流程变量的传递和使用是 Flowable 的核心概念之一。  需要理解流程变量的作用和用法，才能更好地使用 Flowable 引擎。
*   需要处理异常情况，例如任务不存在、用户没有权限等。
*   以上API 接口需要根据你的具体项目配置进行调整，例如请求方式、参数类型等。

希望本教程能够帮助你快速上手 Flowable 引擎，并应用于你的实际项目中。

这个文档包含以下内容：

*   **清晰的标题和介绍**：说明文档的目的和范围。
*   **先决条件**：强调需要准备好的环境和流程定义。
*   **准备工作**：代码片段需要引入的类。
*   **详细的步骤说明**：
    *   接口 URI
    *   请求方法 (POST)
    *   参数说明
    *   功能描述
    *   Java 代码片段
    *   说明和注意事项
*   **流程示例**：模拟整个流程的执行过程。
*   **注意事项**：强调一些重要的点，例如异常处理，流程变量，API调整等。

这篇教程尽可能详细和实用，希望能帮助你或者其他人理解和使用这段 Flowable 代码。


# 本人踩坑：
```xml
<!-- 这里的flowable:assignee值填写的是employee。导致「提交请假申请」的人是静态字符串，一直是“employee”-->
<bpmn:userTask id="applyTask" name="提交请假申请" flowable:assignee="employee"/>


 <!-- 这里的flowable:assignee值填写的是${employee}，才可以动态的根据业务赋值对应的「提交请假申请」人-->
<bpmn:userTask id="applyTask" name="提交请假申请" flowable:assignee="${employee}"/>
```