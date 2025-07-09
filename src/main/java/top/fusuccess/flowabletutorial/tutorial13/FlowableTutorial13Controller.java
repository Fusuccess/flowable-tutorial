package top.fusuccess.flowabletutorial.tutorial13;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/flowable13")
public class FlowableTutorial13Controller {


    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;

    @PostMapping("/listProcessDefinition")
    public List<Map<String, Object>> listProcessDefinition(String processKey) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();
        if (processDefinition != null) {
            instanceInfo.put("processDefinitionId", processDefinition.getId());
            instanceInfo.put("processDefinitionName", processDefinition.getName());
            instanceInfo.put("processDefinitionVersion", processDefinition.getVersion());
        }
        reportList.add(instanceInfo);
        return reportList;
    }







    @PostMapping("/start")
    public List<Map<String, Object>> startProcess(String processKey, String apply, String hrApproval, String leaderApproval) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("apply", apply);
        vars.put("hrApproval", hrApproval);
        vars.put("leaderApproval", leaderApproval);

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, vars);

        instanceInfo.put("instanceId", instance.getId());
        instanceInfo.put("processDefinitionId", instance.getProcessDefinitionId());
        instanceInfo.put("processDefinitionName", instance.getProcessDefinitionName());
        instanceInfo.put("processDefinitionVersion", instance.getProcessDefinitionVersion());
        instanceInfo.put("apply", apply);
        instanceInfo.put("hrApproval", hrApproval);
        instanceInfo.put("leaderApproval", leaderApproval);
        reportList.add(instanceInfo);
        return reportList;
    }
}
