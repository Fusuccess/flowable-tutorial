# Flowable 教程（一）：流程定义（Process Definition）

本教程将介绍 Flowable 工作流引擎的核心概念之一：流程定义（Process Definition），并通过示例帮助你理解如何使用 BPMN 2.0 编写并部署流程。

---

## 什么是流程定义？

流程定义是使用 BPMN 2.0 标准描述的流程蓝图，定义了整个业务流程的节点、顺序、条件、参与人等信息。它是一种静态模型，必须部署到流程引擎后才能实际启动流程实例。

在 Flowable 中，流程定义通常使用 `.bpmn20.xml` 文件表示。

---

## 流程定义的组成

一个典型的 BPMN 流程定义包含以下组成部分：

* 开始事件（startEvent）
* 用户任务（userTask）
* 服务任务（serviceTask）
* 排他网关（exclusiveGateway）
* 顺序流（sequenceFlow）
* 结束事件（endEvent）

---

## 示例流程：请假审批流程

以下是一个最小可运行的请假审批流程：

文件路径：`src/main/resources/dpmn/leave-approve.bpmn20.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:flowable="http://flowable.org/bpmn"
                  targetNamespace="http://www.flowable.org/processdef">

  <bpmn:process id="leaveApproval" name="请假审批流程" isExecutable="true">

    <bpmn:startEvent id="startEvent" name="开始"/>

    <bpmn:userTask id="applyTask" name="提交请假申请" flowable:assignee="employee"/>

    <bpmn:userTask id="approveTask" name="经理审批" flowable:assignee="manager"/>

    <bpmn:endEvent id="endEvent" name="结束"/>

    <bpmn:sequenceFlow id="flow1" sourceRef="startEvent" targetRef="applyTask"/>
    <bpmn:sequenceFlow id="flow2" sourceRef="applyTask" targetRef="approveTask"/>
    <bpmn:sequenceFlow id="flow3" sourceRef="approveTask" targetRef="endEvent"/>

  </bpmn:process>

</bpmn:definitions>
```



---

```mermaid
    A[开始] --> B(提交请假申请);
    B --> C(经理审批);
    C --> D[结束];
```
---

## 常见问题

1. 文件后缀必须为 `.bpmn20.xml` 吗？

    * 是的，Flowable 默认只识别该后缀。

2. 是否可以使用中文？

    * 是的，流程名称、任务名称、节点名均支持中文。

3. 一个文件可以定义多个流程吗？

    * 可以，使用多个 bpmn\:process 标签。

---
