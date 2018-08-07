package com.atomic.tools.mail;

import jodd.mail.Email;
import jodd.mail.EmailMessage;
import jodd.mail.MailServer;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import jodd.net.MimeTypes;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author dreamyao
 * @title
 * @date 2018/8/6 下午4:08
 * @since 1.0.0
 */
public class SmtpMailServer {

    private volatile Email email;

    public void createEmail(MailInfo info) {

        Assert.notNull(info, "mail info parameter is not null.");
        Assert.notNull(info.getSender(), "sender is not null");
        List<String> receivers = info.getReceivers();
        Assert.notNull(receivers, "receivers is not null.");
        List<String> ccUsers = info.getCcUsers();
        Assert.notNull(info.getSubject(), "subject is not null.");
        Assert.notNull(info.getReportInfo(),"report info is not null.");
        email = Email.create();
        email.from(info.getSender());

        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(info.getReceivers()))) {
            for (String receiver : receivers) {
                email.to(receiver);
            }
        }

        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(info.getCcUsers()))) {
            for (String ccUser : ccUsers) {
                email.cc(ccUser);
            }
        }
        email.subject(info.getSubject());

        EmailMessage message = new EmailMessage("Hi All \n\n 以下为自动化测试结果统计信息，请查收！\n"+info.getReportInfo(),MimeTypes.MIME_TEXT_PLAIN,"UTF-8");
        email.message(message);

        // String attachmentContext = new BufferedReader(new InputStreamReader(info.getAttachment())).lines().collect(Collectors.joining(System.lineSeparator()));
        // 发送Html附件，但是附件内容不能以html格式发送，需要以 application/octet-stream 发送
        // email.embeddedAttachment(EmailAttachment.with().name("report.html").content(attachmentContext.getBytes(Charset.defaultCharset()), MimeTypes.MIME_APPLICATION_OCTET_STREAM));

    }

    public void sendEmail() {
        SmtpServer smtpServer;
        SendMailSession session = null;
        try {

            smtpServer = MailServer.create().ssl(true).host("smtphm.qiye.163.com").auth("autotest@primeledger.cn", "zgfQ62T22{kmC[4").buildSmtpMailServer();
            session = smtpServer.createSession();
            session.open();
            session.sendMail(email);

        } finally {
            if (Objects.nonNull(session)) {
                session.close();
            }
        }
    }
}
