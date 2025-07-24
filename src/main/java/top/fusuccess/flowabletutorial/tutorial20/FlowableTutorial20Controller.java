package top.fusuccess.flowabletutorial.tutorial20;

import org.flowable.engine.FormService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.FormInfo;
import org.flowable.form.api.FormInstance;
import org.flowable.form.api.FormModel;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flowable20")
public class FlowableTutorial20Controller {

    @Autowired
    private FormRepositoryService formRepositoryService;

    @Autowired
    private org.flowable.form.api.FormService formService;

    @Autowired
    private org.flowable.engine.FormService formService2;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;



    @GetMapping("/deploy")
    public String deployForm(String formKey) throws IOException {
        String path = "form-definitions/" + formKey + ".form";
        try {
            InputStream is = new ClassPathResource(path).getInputStream();
            formRepositoryService.createDeployment()
                    .name("请假表单部署")
                    .addInputStream(formKey + ".form", is) // 文件名必须以 .form 结尾
                    .deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Form deployed.";
    }

    @GetMapping("/getFormByFormKey")
    public FormModel getFormByFormKey(String formKey) throws IOException {
        FormInfo formInfo = formService.getFormModelWithVariablesByKey(
                formKey,     // 表单key
                null,                // tenantId (可为 null)
                Collections.emptyMap() // 初始变量（通常为空）
        );
        if (formInfo == null) {
            throw new RuntimeException("找不到表单：" + formKey);
        }
        return formInfo.getFormModel();
    }


    @PostMapping("/submit")
    public String submitForm(String formKey, @RequestBody Map<String, Object> values) {
        FormInfo formInfo = formRepositoryService.getFormModelByKey(formKey);

        formService.validateFormFields(formInfo, values);


        FormInstance instance = formService.saveFormInstance(
                values,
                formInfo,
                null,
                null,
                null,
                null,
                null
        );
        return instance.getId();
    }


    @PostMapping("/db")
    public List<Map<String, Object>> deployFromDb(String processKey) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        if (processKey == null || processKey.isEmpty()) {
            info.put("error", "processKey不能为空");
        }
        // 从数据库读取 BPMN XML 字符串
        String sql = "SELECT process_xml FROM flowable_process WHERE process_key = ?";
        List<String> bpmnXmlList = jdbcTemplate.query(sql, new Object[]{processKey}, (rs, rowNum) -> rs.getString("process_xml"));

//            String bpmnXml = jdbcTemplate.queryForObject(sql, new Object[]{processKey}, String.class);

        if (!bpmnXmlList.isEmpty()) {
            String bpmnXml = bpmnXmlList.get(0);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
            Deployment deployment = repositoryService.createDeployment().addInputStream(processKey + ".bpmn20.xml", inputStream).name(processKey).deploy();
            info.put("deployId", deployment.getId());
            info.put("deployName", deployment.getName());
            info.put("deployTime", deployment.getDeploymentTime());
        } else {
            info.put("error", "流程定义文件不存在");
        }

        reportList.add(info);
        return reportList;
    }

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
                info.put("formKey", task.getFormKey());
                reportList.add(info);
            }
        }

        return reportList;
    }

    //提交
    @PostMapping("/submitProperty")
    public List<Map<String, Object>> submitProperty(String taskId, @RequestBody Map<String, String> formData ) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        //提交表单并提交任务写法
        formService2.submitTaskFormData(taskId, formData);

        Map<String, Object> info = new HashMap<>();
        info.put("message", "表单已提交");
        reportList.add(info);
        return reportList;
    }



}
