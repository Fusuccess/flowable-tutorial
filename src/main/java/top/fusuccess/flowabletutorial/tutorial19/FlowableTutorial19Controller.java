package top.fusuccess.flowabletutorial.tutorial19;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flowable19")
public class FlowableTutorial19Controller {

    @Autowired
    private TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @GetMapping("getFormKeyByTaskId/{taskId}")
    public List<Map<String, Object>> getTaskInfo(@PathVariable String taskId) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (task == null) {
            instanceInfo.put("code", HttpStatus.NOT_FOUND.value());
            instanceInfo.put("message", "任务不存在: " + taskId);
            reportList.add(instanceInfo);
            return reportList;
        }

        String formKey = task.getFormKey();

        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("name", task.getName());
        result.put("assignee", task.getAssignee());
        result.put("formKey", formKey);
        result.put("processInstanceId", task.getProcessInstanceId());

        reportList.add(result);
        return reportList;
    }

    //提交
    @PostMapping("/submitProperty")
    public List<Map<String, Object>> submitProperty(String taskId, @RequestBody Map<String, String> formData ) {

        Map<String, Object> variables = new HashMap<>();

        //提交表单并提交任务写法
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            variables.put(entry.getKey(), entry.getValue());
        }
        taskService.complete(taskId, variables);

        //不提交任务只提交表单写法
        formData.forEach((key, value) -> {
            taskService.setVariableLocal(taskId, key, value);
        });

        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        info.put("message", "表单已提交");
        reportList.add(info);
        return reportList;
    }

    @GetMapping("getFormJsonByFormKey/{formKey}")
    public List<Map<String, Object>> getFormByKey(@PathVariable String formKey) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> instanceInfo = new HashMap<>();

        String path = "form-definitions/" + formKey;
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            String rawJson = new String(toByteArray(is), StandardCharsets.UTF_8);
            // ✅ 将 JSON 字符串转为对象
            Object jsonObject = objectMapper.readValue(rawJson, Object.class);
            instanceInfo.put("json", jsonObject);
            reportList.add(instanceInfo);
        } catch (IOException e) {
            instanceInfo.put("code", HttpStatus.NOT_FOUND.value());
            instanceInfo.put("message", "表单不存在: " + formKey);
            reportList.add(instanceInfo);
            return reportList;
        }
        return reportList;
    }

    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}
