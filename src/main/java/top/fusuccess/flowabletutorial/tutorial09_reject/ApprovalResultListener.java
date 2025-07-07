package top.fusuccess.flowabletutorial.tutorial09_reject;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component("ApprovalResultListener")
public class ApprovalResultListener implements TaskListener {
    public ApprovalResultListener() {
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        // 从任务变量获取审批结果
        Object result = delegateTask.getVariable("approvalResult");

        // 提升为流程变量（使网关可以访问）
        if (result != null) {
            delegateTask.setVariable("approvalResult", result);
        }
    }
}