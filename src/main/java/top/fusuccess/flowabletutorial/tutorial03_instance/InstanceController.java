package top.fusuccess.flowabletutorial.tutorial03_instance;

import liquibase.pro.packaged.A;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/instance")
public class InstanceController {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    /**
     * 启动流程实例
     * 说明：
     * startProcessInstanceByKey() 中的 key 是 <bpmn:process id="..."> 中的 ID
     * 第二个参数是流程变量（Map），会传递给 userTask 的 assignee 绑定
     * 启动成功后，实例信息会写入 ACT_RU_EXECUTION 表
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
            System.out.println("当前任务ID：" + task.getId());
            System.out.println("任务名称：" + task.getName());
            System.out.println("当前节点定义ID：" + task.getTaskDefinitionKey());
        }
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

    /**
     * 查询并完成任务
     * 说明：
     * 调用 complete() 会使流程推进到下一节点
     * 如果下一个任务是审批人 “manager”，那么需要切换用户再查任务
     */
    @PostMapping("/taskComplete")
    public void complete(String taskId) {
        taskService.complete(taskId);
    }


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
}
