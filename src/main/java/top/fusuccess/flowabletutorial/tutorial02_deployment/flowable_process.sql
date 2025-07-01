CREATE TABLE flowable_process
(
    id          INT PRIMARY KEY AUTO_INCREMENT,
    process_key VARCHAR(64),
    process_xml TEXT
);
INSERT INTO flowable_process (`id`, `process_key`, `process_xml`)
VALUES (1, 'leaveApproval',
        '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n                  xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n                  xmlns:flowable=\"http://flowable.org/bpmn\"\n                  targetNamespace=\"http://www.flowable.org/processdef\">\n\n    <bpmn:process id=\"leaveApproval\" name=\"请假审批流程_fromSQL\" isExecutable=\"true\">\n\n        <bpmn:startEvent id=\"startEvent\" name=\"开始\"/>\n\n        <bpmn:userTask id=\"applyTask\" name=\"提交请假申请\" flowable:assignee=\"${employee}\"/>\n\n        <bpmn:userTask id=\"approveTask\" name=\"经理审批\" flowable:assignee=\"${manager}\"/>\n\n        <bpmn:endEvent id=\"endEvent\" name=\"结束\"/>\n\n        <bpmn:sequenceFlow id=\"flow1\" sourceRef=\"startEvent\" targetRef=\"applyTask\"/>\n        <bpmn:sequenceFlow id=\"flow2\" sourceRef=\"applyTask\" targetRef=\"approveTask\"/>\n        <bpmn:sequenceFlow id=\"flow3\" sourceRef=\"approveTask\" targetRef=\"endEvent\"/>\n\n    </bpmn:process>\n\n</bpmn:definitions>\n');