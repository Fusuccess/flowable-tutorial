## 踩坑
Flowable 是使用 message name 进行匹配的，而不是 message id！

```xml
<bpmn:message id="startMessageTask" name="启动消息" /> 
```
<message> 上述中定义的 id="startMessageTask"，但是 name="启动消息"。启动流程实例应该为：

```java
runtimeService.startProcessInstanceByMessage("启动消息");
```