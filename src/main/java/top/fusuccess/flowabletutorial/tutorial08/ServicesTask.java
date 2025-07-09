package top.fusuccess.flowabletutorial.tutorial08;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class ServicesTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
            System.out.println("Executing the service task!");
            // 执行你的业务逻辑
            MailSendImpl mailSend = new MailSendImpl();
            mailSend.sendMail("fusuccess@163.com","fusuccess@163.com","测试邮件","测试邮件");
    }

}
