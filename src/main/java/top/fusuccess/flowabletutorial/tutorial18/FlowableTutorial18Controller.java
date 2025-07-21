package top.fusuccess.flowabletutorial.tutorial18;

import org.flowable.engine.FormService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.form.TaskFormData;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flowable18")
public class FlowableTutorial18Controller {

    @Autowired
    private FormService formService;


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @PostMapping("/getProperty")
    public List<Map<String, Object>> getStartForm(String taskId) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();

        TaskFormData taskFormData = formService.getTaskFormData(taskId);
        if (taskFormData == null) {
            info.put("error", "任务表单不存在");
            reportList.add(info);
            return reportList;
        }
        List<FormProperty> formProperties = taskFormData.getFormProperties();

        List<FormPropertyRepresentation> formReportList = new ArrayList<>();
        // 构建返回结果
        for (FormProperty formProperty : formProperties) {

            FormPropertyRepresentation formPropertyRepresentation = new FormPropertyRepresentation();
            formPropertyRepresentation.setId(formProperty.getId());
            formPropertyRepresentation.setName(formProperty.getName());
            formPropertyRepresentation.setType(formProperty.getType());
            formPropertyRepresentation.setRequired(formProperty.isRequired());
            formPropertyRepresentation.setValue(formProperty.getValue());
            formReportList.add(formPropertyRepresentation);
        }
        reportList.add(new HashMap(){{put("form", formReportList);}});
        return reportList;
    }

    //提交
    @PostMapping("/submitProperty")
    public List<Map<String, Object>> submitProperty(String taskId, @RequestBody Map<String, String> formData ) {
        List<Map<String, Object>> reportList = new ArrayList<>();

        //提交表单并提交任务写法
        formService.submitTaskFormData(taskId, formData);

        //不提交任务只提交表单写法
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        formData.forEach((key, value) -> {
            runtimeService.setVariable(processInstanceId, key, value);
        });

        Map<String, Object> info = new HashMap<>();
        info.put("message", "表单已提交");
        reportList.add(info);
        return reportList;
    }

    //获取表单内容
    @PostMapping("/getPropertyValue")
    public List<Map<String, Object>> getPropertyValue(String procInstId) {
        // 获取流程变量
        Object reason = runtimeService.getVariable(procInstId, "reason");

        Map<String, Object> variables = runtimeService.getVariables(procInstId);

        List<Map<String, Object>> reportList = new ArrayList<>();
        reportList.add(new HashMap(){{put("指定获取流程变量reason", reason);}});
        reportList.add(new HashMap(){{put("获取所有流程变量variables", variables);}});

        return reportList;
    }

}
