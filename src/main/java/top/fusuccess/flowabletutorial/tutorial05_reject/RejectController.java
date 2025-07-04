package top.fusuccess.flowabletutorial.tutorial05_reject;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave")
public class RejectController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    // 1. 发起请假流程（启动流程）
    @PostMapping("/start")
    public List<Map<String, Object>> startProcess(@RequestParam String processKey,@RequestParam String employee, @RequestParam String manager) {
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

    // 2. 查询用户任务
    @GetMapping("/tasks")
    public List<Task> getUserTasks(@RequestParam String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
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

    // 3. 完成任务（通用方法）
    @PostMapping("/complete")
    public List<Map<String, Object>> completeTask(@RequestParam String taskId,
                               @RequestParam(required = false) String approvalResult) {
        Map<String, Object> variables = new HashMap<>();
        if (approvalResult != null) {
            variables.put("approvalResult", approvalResult);
        }
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        taskService.complete(taskId, variables);

        info.put("success", "任务完成");
        reportList.add(info);
        return reportList;
    }
}