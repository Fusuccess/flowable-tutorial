package top.fusuccess.flowabletutorial.tutorial05;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flowable05")
public class FlowableTutorial05Controller {
    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @PostMapping("/taskComplete")
    public List<Map<String, Object>> submitLeaveTask(String taskId,
                                                     @RequestBody Map<String, Object> formData) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();

        try {
            // 示例 formData: {"days":3,"reason":"出差"}
            taskService.setVariablesLocal(taskId, formData);
        } catch (Exception e) {
            e.printStackTrace();
            info.put("error", "提交表单数据失败");
            reportList.add(info);
        }
        taskService.complete(taskId, formData);

        return reportList;
    }

    @GetMapping("/taskMessage/{processInstanceId}")
    public List<Map<String, Object>> taskMessage(@PathVariable String processInstanceId) {
        List<Map<String, Object>> reportList = new ArrayList<>();

        Map<String, Object> vars = null;
        // 优先查运行时变量
        if (runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult() != null) {
            vars = runtimeService.getVariables(processInstanceId);

            reportList.add(vars);
        }
        return reportList;

//        @GetMapping("/historyMessage/{processInstanceId}")
//        public Map<String, List<Object>> historyMessage (@PathVariable String processInstanceId){
//
//            Map<String, List<Object>> varMap = null;
//            // 优先查运行时变量
//            varMap = historyService.createHistoricVariableInstanceQuery()
//                    .processInstanceId(processInstanceId)
//                    .list()
//                    .stream()
//                    .collect(Collectors.groupingBy(
//                            HistoricVariableInstance::getVariableName,
//                            Collectors.mapping(HistoricVariableInstance::getValue, Collectors.toList())
//                    ));
//            return varMap;
        }
/**
 * 根据taskId查询所在流程
 */

    }
