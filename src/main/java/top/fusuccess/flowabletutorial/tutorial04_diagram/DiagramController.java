package top.fusuccess.flowabletutorial.tutorial04_diagram;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
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
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/diagram")
public class DiagramController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessDiagramGenerator processDiagramGenerator;

    /**

     渲染动态流程图（高亮当前任务）
     */
    @GetMapping(value = "/runtime/{processInstanceId}", produces = MediaType.IMAGE_PNG_VALUE)
    public void getRuntimeDiagram(@PathVariable String processInstanceId, HttpServletResponse response) throws IOException {

        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        BpmnModel model = repositoryService.getBpmnModel(instance.getProcessDefinitionId());

        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);

        InputStream in = processDiagramGenerator.generateDiagram(
                model,
                "png",
                activeActivityIds,
                Collections.emptyList(),
                "宋体", "宋体", "宋体",
                null, 1.0, false);

        StreamUtils.copy(in, response.getOutputStream());
    }
}
