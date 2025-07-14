package top.fusuccess.flowabletutorial.tutorial16;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("ApprovalResult16Listener")
public class ApprovalResult16Listener implements TaskListener {
    public ApprovalResult16Listener() {
    }


    @Override
    public void notify(DelegateTask delegateTask) {
        // 获取当前任务的审批结果
        String approvalResult = (String) delegateTask.getVariable("approvalResult");

        // 获取流程变量中的审批结果集合（直接通过DelegateTask获取）
        @SuppressWarnings("unchecked")
        List<String> approvalResults = (List<String>) delegateTask.getVariable("approvalResults");

        // 如果集合为空，初始化一个新的列表
        if (approvalResults == null) {
            approvalResults = new ArrayList<>();
        }

        // 将当前审批结果加入集合
        approvalResults.add(approvalResult);

        // 将更新后的审批结果集合重新设置为流程变量
        delegateTask.setVariable("approvalResults", approvalResults);

        System.out.println("任务 " + delegateTask.getName() + " 审批结果: " + approvalResult);
    }
}