package top.fusuccess.flowabletutorial.tutorial15;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flowable15")
public class FlowableTutorial15Controller {
    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;
    @PostMapping("/start")
    public List<Map<String, Object>> startProcess(String processKey, String applyUser, String managerApprovalUser, String hrApprovalUser) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("applyUser", applyUser);
        vars.put("managerApprovalUser", managerApprovalUser);
        vars.put("hrApprovalUser", hrApprovalUser);

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, vars);

        instanceInfo.put("instanceId", instance.getId());
        instanceInfo.put("processDefinitionId", instance.getProcessDefinitionId());
        instanceInfo.put("processDefinitionName", instance.getProcessDefinitionName());
        instanceInfo.put("processDefinitionVersion", instance.getProcessDefinitionVersion());
        instanceInfo.put("applyUser", applyUser);
        instanceInfo.put("managerApprovalUser", managerApprovalUser);
        instanceInfo.put("hrApprovalUser", hrApprovalUser);
        reportList.add(instanceInfo);
        return reportList;
    }


    @PostMapping("/complete")
    public List<Map<String, Object>> complete(String taskId, Boolean moveActivityFlg, String moveActivityIdTo) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        if (moveActivityFlg) {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            String instanceId = task.getProcessInstanceId();
            //查询当前任务节点ID
            ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();

            if (instance == null) {
                instanceInfo.put("code", HttpStatus.NOT_FOUND.value());
                instanceInfo.put("message", "流程实例不存在: " + instanceId);
                reportList.add(instanceInfo);
                return reportList;
            }
            String currentActivityId = null;

            String executionId = task.getExecutionId();
            currentActivityId = runtimeService.getActiveActivityIds(executionId).get(0);

            if (currentActivityId == null) {
                instanceInfo.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
                instanceInfo.put("message", "流程实例不在活动节点上");
                reportList.add(instanceInfo);
                return reportList;
            }

            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(instanceId)
                    .moveActivityIdTo(currentActivityId, moveActivityIdTo)
                    .changeState();

            // 验证流程是否回到了moveActivityIdTo节点
            Task applicantTask = taskService.createTaskQuery().processInstanceId(instanceId).taskDefinitionKey(moveActivityIdTo).singleResult();
            if (applicantTask != null) {
                if (task != null) {
                    instanceInfo.put("任务已成功驳回到目标节点: ", applicantTask.getId());
                    instanceInfo.put("任务处理人: ", applicantTask.getAssignee());
                    instanceInfo.put("任务候选组: ", applicantTask.getOwner());
                } else {
                    instanceInfo.put("ERROR", "任务未成功驳回到目标节点");
                }
                instanceInfo.put("message", "流程已成功驳回到" + moveActivityIdTo + "节点: " + applicantTask.getId());
            }
        } else {
            taskService.complete(taskId);
            instanceInfo.put("message", "任务已处理完成");
        }
        reportList.add(instanceInfo);
        return reportList;
    }


}
