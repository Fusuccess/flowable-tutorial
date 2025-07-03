package top.fusuccess.flowabletutorial.tutorial03_instance;

import liquibase.pro.packaged.S;
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
    public List<Map<String, Object>> start(String processKey, String employee, String manager) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("employee", employee);
        vars.put("manager", manager);

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, vars);

        instanceInfo.put("instanceId", instance.getId());
        instanceInfo.put("processDefinitionId", instance.getProcessDefinitionId());
        instanceInfo.put("processDefinitionName", instance.getProcessDefinitionName());
        instanceInfo.put("processDefinitionVersion", instance.getProcessDefinitionVersion());
        instanceInfo.put("employee", employee);
        instanceInfo.put("manager", manager);
        reportList.add(instanceInfo);
        return reportList;
    }

    /**
     * 查询流程实例
     */
    @PostMapping("/instanceList")
    public List<Map<String, Object>> instanceList(String key) {
        List<ProcessInstance> instances = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(key)
                .active()
                .list();

        List<Map<String, Object>> reportList = new ArrayList<>();
        for (ProcessInstance pi : instances) {
            Map<String, Object> instanceInfo = new HashMap<>();
            instanceInfo.put("instanceId", pi.getId());
            instanceInfo.put("processDefinitionId", pi.getProcessDefinitionId());
            instanceInfo.put("processDefinitionName", pi.getProcessDefinitionName());
            instanceInfo.put("processDefinitionVersion", pi.getProcessDefinitionVersion());
            instanceInfo.put("processDefinitionKey", pi.getProcessDefinitionKey());
            instanceInfo.put("startTime", pi.getStartTime());
            reportList.add(instanceInfo);
        }
        return reportList;
    }

    /**
     * 查询当前任务
     */
    @PostMapping("/taskListByUser")
    public List<Map<String, Object>>  taskListByUser(String processKey, String user) {
        List<Task> tasks = taskService
                .createTaskQuery()
                .processDefinitionKey(processKey)
                .taskAssignee(user)
                .list();

        List<Map<String, Object>> reportList = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> info = new HashMap<>();
            info.put("taskId", task.getId());
            info.put("taskName", task.getName());
            info.put("taskDefinitionKey", task.getTaskDefinitionKey());
            info.put("assignee", task.getAssignee());
            info.put("processInstanceId", task.getProcessInstanceId());
            reportList.add(info);
        }

        return reportList;
    }

    /**
     * 查询当前任务
     */
    @PostMapping("/taskListByInstanceId")
    public List<Map<String, Object>> taskListByInstanceId(String instanceId) {
        List<Map<String, Object>> reportList = new ArrayList<>();

        List<Task> tasks = taskService
                .createTaskQuery()
                .processInstanceId(instanceId)
                .list();

        for (Task task : tasks) {
            Map<String, Object> info = new HashMap<>();
            info.put("taskId", task.getId());
            info.put("taskName", task.getName());
            info.put("taskDefinitionKey", task.getTaskDefinitionKey());
            info.put("processInstanceId", task.getProcessInstanceId());
            info.put("assignee", task.getAssignee());
            reportList.add(info);
        }
        return reportList;
    }

    /**
     * 完成任务
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
    public List<Map<String, Object>> historyList() {
        List<Map<String, Object>> reportList = new ArrayList<>();

        List<HistoricProcessInstance> finished = historyService
                .createHistoricProcessInstanceQuery()
                .finished().orderByProcessInstanceEndTime()
                .desc()
                .list();

        List<String> historyList = new ArrayList<>();
        for (HistoricProcessInstance h : finished) {
            Map<String, Object> info = new HashMap<>();
            info.put("instanceId", h.getId());
            info.put("processDefinitionId", h.getProcessDefinitionId());
            info.put("processDefinitionName", h.getProcessDefinitionName());
            info.put("processDefinitionVersion", h.getProcessDefinitionVersion());
            info.put("startTime", h.getStartTime());
            info.put("endTime", h.getEndTime());
            info.put("duration", h.getDurationInMillis());
            reportList.add(info);
        }
        return reportList;
    }
}
