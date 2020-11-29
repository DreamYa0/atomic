package com.atomic.config;


import com.atomic.tools.mock.data.TestMode;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author dreamyao
 * @title
 * @date 16/5/23 下午7:55
 * @since 1.0.0
 */
public class TesterConfig {

    private static final String TEST_CONFIG_FILE_PATH = "/test.properties";
    private static TesterConfigEntity entity = new TesterConfigEntity();

    static {
        load();
    }

    private static void load() {

        InputStream in = FileUtils.getTestFileInputStream(TEST_CONFIG_FILE_PATH);
        if (Objects.isNull(in)) {
            return;
        }

        try {

            Properties properties = new Properties();
            properties.load(in);

            if (properties.containsKey("profile")) {
                String env = properties.getProperty("profile");
                entity.setProfile((env == null || "".equals(env)) ? TestMode.T1.getName() : env);
            }
            if (properties.containsKey("mail.addr.list")) {
                entity.setMailAddrList(splitter(properties.getProperty("mail.addr.list")));
            }
            if (properties.containsKey("host.domain")) {
                entity.setHostDomain(properties.getProperty("host.domain"));
            }
            if (properties.containsKey("project.name") &&
                    Boolean.FALSE.equals(StringUtils.isEmpty(properties.getProperty("project.name")))) {
                entity.setProjectName(properties.getProperty("project.name"));
            }
            if (properties.containsKey("runner") &&
                    Boolean.FALSE.equals(StringUtils.isEmpty(properties.getProperty("runner")))) {
                entity.setRunner( properties.getProperty("runner"));
            }
            if (properties.containsKey("run.mode")) {
                entity.setRunMode(properties.getProperty("run.mode"));
            }
            if (properties.containsKey("service.version")) {
                entity.setServiceVersion(properties.getProperty("service.version"));
            }
            if (properties.containsKey("host")) {
                entity.setHost(properties.getProperty("host"));
            }
            if (properties.containsKey("header")) {
                entity.setHeader(properties.getProperty("header"));
            }

        } catch (Exception e) {
            throw new RuntimeException("配置文件参数非法,请检查test.properties的配置", e);
        }
    }

    @SuppressWarnings("all")
    private static List<String> splitter(String from) {
        return Splitter.on(",").splitToList(from);
    }

    public static String getServiceVersion() {
        return entity.getServiceVersion();
    }

    public static String getProfile() {
        return entity.getProfile();
    }

    public static String getHttpHost() {
        return entity.getHost();
    }

    public static String getHostDomain() {
        return entity.getHostDomain();
    }

    public static String getRunner() {
        return entity.getRunner();
    }

    public static void setRunner(String runner) {
        entity.setRunner(runner);
    }

    public static String getProjectName() {
        return entity.getProjectName();
    }

    public static void setProjectName(String projectName) {
        entity.setProjectName(projectName);
    }

    public static String getHeader() {
        return entity.getHeader();
    }
}
