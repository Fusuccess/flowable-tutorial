package top.fusuccess.flowabletutorial.tutorial04_diagram;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.bpmn.model.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/diagram")
public class DiagramController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessDiagramGenerator diagramGenerator;

    /**
     * 渲染动态流程图（高亮当前任务）
     */
    @GetMapping(value = "/runtime/{processInstanceId}", produces = MediaType.IMAGE_PNG_VALUE)
    public void getRuntimeDiagram(@PathVariable String processInstanceId, HttpServletResponse response) throws IOException {

        List<String> activeActivityIds = new ArrayList<>();

        String processDefinitionId;

        // 尝试查询运行时流程实例
        ProcessInstance runtimeInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (runtimeInstance != null) {
            // 运行中的流程：获取当前活动节点
            activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
            processDefinitionId = runtimeInstance.getProcessDefinitionId();
        } else {
            // 查询历史流程实例
            HistoricProcessInstance historicInstance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (historicInstance == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("流程实例不存在！");
                return;
            }

            processDefinitionId = historicInstance.getProcessDefinitionId();

            // 历史节点：回放执行轨迹（按执行顺序）
            List<HistoricActivityInstance> historyNodes = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderByHistoricActivityInstanceStartTime().asc()
                    .list();

            activeActivityIds = historyNodes.stream()
                    .map(HistoricActivityInstance::getActivityId)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 加载流程模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<String> flowIds = getHighLightedFlows(bpmnModel, activeActivityIds);

        InputStream diagram = diagramGenerator.generateDiagram(
                bpmnModel,
                "png",
                activeActivityIds,
                flowIds,
                "宋体", "宋体", "宋体",
                null, 1.0, false
        );

        StreamUtils.copy(diagram, response.getOutputStream());
    }

    /**

     计算已走过的流程线条（flowId）
     */
    private List<String> getHighLightedFlows(BpmnModel bpmnModel, List<String> historicActivityIds) {
        List<String> highLightedFlows = new ArrayList<>();
        List<FlowElement> allElements = new ArrayList<>();

        for (Process process : bpmnModel.getProcesses()) {
            allElements.addAll(process.getFlowElements());
        }

        Map<String, FlowNode> nodeMap = allElements.stream()
                .filter(e -> e instanceof FlowNode)
                .collect(Collectors.toMap(FlowElement::getId, e -> (FlowNode) e));

        for (String historicId : historicActivityIds) {
            FlowNode currentNode = nodeMap.get(historicId);
            if (currentNode == null) continue;
            for (SequenceFlow flow : currentNode.getOutgoingFlows()) {
                String targetRef = flow.getTargetRef();
                if (historicActivityIds.contains(targetRef)) {
                    highLightedFlows.add(flow.getId());
                }
            }
        }

        return highLightedFlows;
    }
}
