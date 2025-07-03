package top.fusuccess.flowabletutorial.tutorial02_deployment;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/deploy")
public class DeploymentController {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/file")
    public List<Map<String, Object>> deployFromFile(String processKey) {
        List<Map<String, Object>> reportList = new ArrayList<>();
        Map<String, Object> info = new HashMap<>();
        if (processKey == null || processKey.isEmpty()) {
            info.put("error", "processKey不能为空");
        }

        String filePath = "bpmn/" + processKey + ".bpmn20.xml";
        URL resourceUrl = getClass().getClassLoader().getResource(filePath);

        if (resourceUrl != null) {
            Deployment deployment = repositoryService.createDeployment()
                    .addClasspathResource("bpmn/" + processKey + ".bpmn20.xml")
                    .name(processKey)
                    .deploy();

            info.put("deployId", deployment.getId());
            info.put("deployName", deployment.getName());
            info.put("deployTime", deployment.getDeploymentTime());

        } else {
            info.put("error", "流程定义文件不存在");
        }

        reportList.add(info);
        return reportList;
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
}
