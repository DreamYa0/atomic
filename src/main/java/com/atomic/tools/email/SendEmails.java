package com.atomic.tools.email;

import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

import javax.mail.internet.InternetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2018/05/30 10:48
 */
public class SendEmails {

    /**
     * 邮件配置
     */
    private final Map<String, String> CONFIG = getProperties();
    /**
     * 默认编码
     */
    private final String UTF8 = "utf-8";
    /**
     * 域名
     */
    private String DOMAIN = CONFIG.get("mail.domain");
    /**
     * 发送服务器地址
     */
    private String HOST = CONFIG.get("mail.host");
    /**
     * 发送端口
     */
    private String PORT = CONFIG.get("mail.port");
    /**
     * 用户名
     */
    private String USER = CONFIG.get("mail.user");
    /**
     * 密码
     */
    private String PASSWORD = CONFIG.get("mail.password");
    /**
     * 发送者
     */
    private String SENDER = CONFIG.get("mail.sender");

    /**
     * 获取邮件domain
     * @return 返回domain
     */
    public String getMailDomain() {
        return DOMAIN;
    }

    /**
     * 获取admin的邮箱
     * @return admin邮箱地址
     */
    public String getAdminMail() {
        return USER + "@" + DOMAIN;
    }

    /**
     * 发送邮件
     * @param receives 收件人，多个收件人以;隔开
     * @param subject  邮件主题
     * @param msg      邮件内容
     * @throws Exception 发送邮件异常
     */
    public void send(String receives, String subject, String msg) throws Exception {
        HtmlEmail htmlEmail = new HtmlEmail();
        htmlEmail.setHostName(HOST);
        htmlEmail.setSmtpPort(Integer.parseInt(PORT));
        htmlEmail.setAuthentication(USER, PASSWORD);
        htmlEmail.setFrom(SENDER);
        htmlEmail.setTo(getSendAddressList(receives));
        htmlEmail.setCharset(UTF8);
        htmlEmail.setSubject(subject);
        htmlEmail.setMsg(msg);
        htmlEmail.send();
    }

    public void sendEmailAttachment(String receives, String subject, String msg) throws Exception {
        EmailAttachment attachment = new EmailAttachment();
        attachment.setName("自动化测试报告");
        attachment.setDescription("自动化测试报告");
        //邮件附件地址
        String ATTACHMENT_PATH = "src/test/resources/report/report.html";
        attachment.setPath(ATTACHMENT_PATH);
        HtmlEmail htmlEmail = new HtmlEmail();
        htmlEmail.setHostName(HOST);
        htmlEmail.setSmtpPort(Integer.parseInt(PORT));
        htmlEmail.setAuthentication(USER, PASSWORD);
        htmlEmail.setFrom(SENDER);
        htmlEmail.setTo(getSendAddressList(receives));
        htmlEmail.setCharset(UTF8);
        htmlEmail.setSubject(subject);
        htmlEmail.setMsg(msg);
        htmlEmail.attach(attachment);
        htmlEmail.send();
    }

    /**
     * 格式化邮件内容
     * @param template 邮件模板
     * @param args     参数
     * @return 返回实际的邮件内容
     */
    public String format(String template, String[] args) {
        MessageFormat format = new MessageFormat(template);
        return format.format(args);
    }

    /**
     * 组装收件人
     * @param receives 收件人
     * @return 真实的收件人地址
     * @throws Exception 拼装邮件地址异常
     */
    private List<InternetAddress> getSendAddressList(String receives) throws Exception {
        List<InternetAddress> addressList = new ArrayList<>();
        String[] tokens = receives.split(";");
        for (String address : tokens) {
            addressList.add(new InternetAddress(address));
        }
        return addressList;
    }

    private Map<String, String> getProperties() {
        GlobalConfig.load();
        //配置文件地址
        return CenterConfig.newInstance().readPropertyConfig(GlobalConfig.getProfile());
    }
}
