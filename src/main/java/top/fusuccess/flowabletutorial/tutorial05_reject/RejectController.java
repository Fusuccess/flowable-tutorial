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

    @PostMapping("/complete")
    public List<Map<String, Object>> completeTask(@RequestParam String taskId,String approvalResult) {
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