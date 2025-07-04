package top.fusuccess.flowabletutorial.tutorial11;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component("TimerStartEventListener")
public class TimerStartEventListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        execution.setVariable("employee", "符元学");
        execution.setVariable("manager", "曾");
    }
}