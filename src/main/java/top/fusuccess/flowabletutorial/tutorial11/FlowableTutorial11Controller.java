package top.fusuccess.flowabletutorial.tutorial11;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flowable11")
public class FlowableTutorial11Controller {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;

    /**
     * 查询当前任务
     */
    @PostMapping("/taskListByUser")
    public List<Map<String, Object>> taskListByUser(String processKey, String user) {
        List<Task> tasks = taskService
                .createTaskQuery()
                .processDefinitionKey(processKey)
                .taskAssignee(user)
                .list();

        List<Map<String, Object>> reportList = new ArrayList<>();
        for (Task task : tasks) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            if (processInstance != null && !processInstance.isSuspended()) {

                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(processInstance.getProcessDefinitionId())
                        .singleResult();

                Map<String, Object> info = new HashMap<>();
                if (processDefinition != null) {
                    info.put("processDefinitionId", processDefinition.getId());
                }
                info.put("taskId", task.getId());
                info.put("taskName", task.getName());
                info.put("taskDefinitionKey", task.getTaskDefinitionKey());
                info.put("assignee", task.getAssignee());
                info.put("processInstanceId", task.getProcessInstanceId());
                reportList.add(info);
            }
        }

        return reportList;
    }


    /**
     * 暂停流程定义
     */
    @PostMapping("/stopProcessDefinition")
    public List<Map<String, Object>> stopProcessDefinition(String processDefinitionKey, Integer version, Boolean suspendProcessInstances) {

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionVersion(version)
                .singleResult();
        if (processDefinition != null) {
            repositoryService.suspendProcessDefinitionById(
                    processDefinition.getId(), // 使用流程定义 ID
                    suspendProcessInstances,
                    null
            );
        }
        instanceInfo.put("message", "流程定义已暂停");
        reportList.add(instanceInfo);
        return reportList;
    }


    /**
     * 激活流程定义
     */
    @PostMapping("/activateProcessDefinition")
    public List<Map<String, Object>> activateProcessDefinition(String processDefinitionKey, Integer version, Boolean
            includeProcessInstances) {

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionVersion(version)
                .singleResult();
        if (processDefinition != null) {
            repositoryService.activateProcessDefinitionById(
                    processDefinition.getId(), // 使用流程定义 ID
                    includeProcessInstances,
                    null
            );
        }
        instanceInfo.put("message", "流程定义已激活");
        reportList.add(instanceInfo);
        return reportList;
    }

    /**
     * 删除流程定义
     */
    @PostMapping("/removeProcessDefinition")
    public List<Map<String, Object>> removeProcessDefinition(String deployId, Boolean cascade) {

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        repositoryService.deleteDeployment(deployId, cascade); // true 表示级联删除，同时删除相关的流程实例

        instanceInfo.put("message", "流程定义已删除");
        reportList.add(instanceInfo);
        return reportList;
    }
}
