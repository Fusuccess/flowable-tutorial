package top.fusuccess.flowabletutorial.tutorial18;

import org.flowable.engine.FormService;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.form.TaskFormData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
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

}
