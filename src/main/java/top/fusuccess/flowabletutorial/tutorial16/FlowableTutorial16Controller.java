package top.fusuccess.flowabletutorial.tutorial16;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/flowable16")
public class FlowableTutorial16Controller {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @PostMapping("/start")
    public List<Map<String, Object>> start(String processKey, String employee, @RequestBody List<String> manager) {
        // 1. 传入动态审批人列表
        Map<String, Object> vars = new HashMap<>();
        vars.put("employee", employee);
        vars.put("approverList", manager);

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, vars);

        instanceInfo.put("instanceId", instance.getId());
        instanceInfo.put("processDefinitionId", instance.getProcessDefinitionId());
        instanceInfo.put("processDefinitionName", instance.getProcessDefinitionName());
        instanceInfo.put("processDefinitionVersion", instance.getProcessDefinitionVersion());
        instanceInfo.put("employee", employee);
        instanceInfo.put("approverList", manager);
        reportList.add(instanceInfo);
        return reportList;
    }

    @PostMapping("/complete")
    public List<Map<String, Object>> completeTask(@RequestParam String taskId, String approvalResult) {
        Map<String, Object> variables = new HashMap<>();
        if (approvalResult != null) {
            variables.put("approvalResult", approvalResult);
        }
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        taskService.complete(taskId, variables);

        info.put("success", "任务完成");
        info.put("taskId", taskId);
        info.put("approvalResult", approvalResult);
        reportList.add(info);
        return reportList;
    }
}
