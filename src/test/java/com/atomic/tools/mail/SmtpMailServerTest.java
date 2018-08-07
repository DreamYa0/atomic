package com.atomic.tools.mail;

import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Collections;

/**
 * @author dreamyao
 * @title
 * @date 2018/8/6 下午7:39
 * @since 1.0.0
 */
public class SmtpMailServerTest {

    @Test
    public void testSendEmail() throws Exception {

        InputStream stream = SmtpMailServerTest.class.getResourceAsStream("/report/report.html");

        String reportInfo= "total: " + 100 +
                "\n" +
                "success: " + 90 +
                "\n" +
                "fail: " + 10 +
                "\n" +
                "skip: " + 0 +
                "\n" +
                "casenum: " + 100 +
                "\n" +
                "successRate: " + 90 +
                "\n" +
                "status: " + (10 == 0 ? "Successful" : "Failed") +
                "\n" +
                "reportUrl: " + "http://10.200.173.92:1337/#/report-summary?id=5b5ebe638a5511768cb05ae4";

        MailInfo info = new MailInfo();
        info.setSender("autotest@primeledger.cn");
        info.setReceivers(Collections.singletonList("yaojun@primeledger.cn"));
        info.setSubject("自动化测试报告");
        info.setReportInfo(reportInfo);
        SmtpMailServer smtpMailServer = new SmtpMailServer();
        smtpMailServer.createEmail(info);
        smtpMailServer.sendEmail();
    }

}