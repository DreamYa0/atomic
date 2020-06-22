package com.atomic.config;

import java.io.Serializable;
import java.util.List;

/**
 * @author dreamyao
 * @title
 * @date 2020/6/22 9:23 PM
 * @since 1.0.0
 */
public class ConfigEntity implements Serializable {

    private static final long serialVersionUID = 2742985008776382196L;

    private String projectName = "";
    private String runner = "";
    private String runMode = "debug";
    private String hostDomain;
    private String serviceVersion;
    private String profile = "";
    private String httpHost = "";
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

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public List<String> getMailAddrList() {
        return mailAddrList;
    }

    public void setMailAddrList(List<String> mailAddrList) {
        this.mailAddrList = mailAddrList;
    }
}
