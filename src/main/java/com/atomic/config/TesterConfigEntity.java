package com.atomic.config;

import java.io.Serializable;
import java.util.List;

/**
 * @author dreamyao
 * @title
 * @date 2020/6/22 9:23 PM
 * @since 1.0.0
 */
public class TesterConfigEntity implements Serializable {

    private static final long serialVersionUID = 2742985008776382196L;

    /**
     * 项目名称
     */
    private String projectName = "";
    /**
     * 执行人
     */
    private String runner = "";
    private String runMode = "debug";
    private String hostDomain;
    /**
     * dubbo服务版本
     */
    private String serviceVersion;
    /**
     * 环境
     */
    private String profile = "";
    /**
     * http 请求地址
     */
    private String host = "";
    /**
     * http 请求header
     */
    private String header;
    private List<String> mailAddrList;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRunner() {
        return runner;
    }

    public void setRunner(String runner) {
        this.runner = runner;
    }

    public String getRunMode() {
        return runMode;
    }

    public void setRunMode(String runMode) {
        this.runMode = runMode;
    }

    public String getHostDomain() {
        return hostDomain;
    }

    public void setHostDomain(String hostDomain) {
        this.hostDomain = hostDomain;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getMailAddrList() {
        return mailAddrList;
    }

    public void setMailAddrList(List<String> mailAddrList) {
        this.mailAddrList = mailAddrList;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
