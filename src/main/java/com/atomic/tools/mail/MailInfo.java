package com.atomic.tools.mail;

import java.util.List;

/**
 * @author dreamyao
 * @title 封装邮件信息对象
 * @date 2018/8/6 下午3:45
 * @since 1.0.0
 */
public class MailInfo {

    /**
     * 发件人
     */
    private String sender;
    /**
     * 收件人列表
     */
    private List<String> receivers;
    /**
     * 抄送人列表
     */
    private List<String> ccUsers;
    /**
     * 邮件标题
     */
    private String subject;

    /**
     * 测试结果统计信息
     */
    private String reportInfo;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public List<String> getCcUsers() {
        return ccUsers;
    }

    public void setCcUsers(List<String> ccUsers) {
        this.ccUsers = ccUsers;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReportInfo() {
        return reportInfo;
    }

    public void setReportInfo(String reportInfo) {
        this.reportInfo = reportInfo;
    }
}
