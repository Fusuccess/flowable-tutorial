package top.fusuccess.flowabletutorial.tutorial10;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flowable10")
public class FlowableTutorial10Controller {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @PostMapping("/start")
    public List<Map<String, Object>> start(String startMessage, String employee, String manager) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("employee", employee);
        vars.put("manager", manager);

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        ProcessInstance instance = runtimeService.startProcessInstanceByMessage(startMessage, vars);

        instanceInfo.put("instanceId", instance.getId());
        instanceInfo.put("processDefinitionId", instance.getProcessDefinitionId());
        instanceInfo.put("processDefinitionName", instance.getProcessDefinitionName());
        instanceInfo.put("processDefinitionVersion", instance.getProcessDefinitionVersion());
        instanceInfo.put("employee", employee);
        instanceInfo.put("manager", manager);
        reportList.add(instanceInfo);
        return reportList;
    }
}
