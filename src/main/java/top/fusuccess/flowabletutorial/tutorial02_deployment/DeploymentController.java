package top.fusuccess.flowabletutorial.tutorial02_deployment;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/deploy")
public class DeploymentController {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/local")
    public String deployFromLocal() {
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leave-approve.bpmn20.xml")
                .name("请假流程部署")
                .deploy();

        System.out.println("部署ID：" + deployment.getId());
        return "部署成功";
    }

    @PostMapping("/db")
    public String deployFromDb() {
        // 从数据库读取 BPMN XML 字符串
        String sql = "SELECT process_xml FROM flowable_process WHERE process_key = ?";
        String processKey = "leaveApproval";

        String bpmnXml = jdbcTemplate.queryForObject(sql, new Object[]{processKey}, String.class);

        if (bpmnXml != null && !bpmnXml.isEmpty()) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));

            Deployment deployment = repositoryService.createDeployment()
                    .addInputStream(processKey + ".bpmn20.xml", inputStream)
                    .name("数据库流程部署：" + processKey)
                    .deploy();

            System.out.println("数据库流程部署成功，部署ID：" + deployment.getId());
        } else {
            System.out.println("未找到流程定义 XML");
        }
        return "部署成功";
    }
}
