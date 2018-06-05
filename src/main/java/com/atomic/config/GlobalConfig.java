package com.atomic.config;


import com.atomic.enums.TestMode;
import com.atomic.util.FileUtils;
import com.google.common.collect.Lists;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author dreamyao
 * @title
 * @date 16/5/23 下午7:55
 * @since 1.0.0
 */
public class GlobalConfig {

    private static final String testConfFilePath = "/test.properties";
    public static String projectName = "";
    public static String runner = "";
    public static String runMode = "debug";
    public static List<String> casePriorityList, caseTypeList, mailAddrList; //执行用例的list
    public static List<String> scanPackageList = Lists.newArrayList();//自动扫描的包目录
    public static String hostDomain = null;
    private static String serviceVersion = null;
    private static String profile = "";
    private static String httpHost = "";

    public static void load() {
        try {
            InputStream in = FileUtils.getTestFileInputStream(testConfFilePath);
            Properties properties = new Properties();
            properties.load(in);
            if (properties.containsKey("profile")) {
                String env = properties.getProperty("profile");
                if (env == null || "".equals(env)) {
                    profile = TestMode.TEST_ONE.getName();
                } else {
                    profile = env;
                }
            }
            if (properties.containsKey("mailAddrList")) {
                mailAddrList = getListFromString(properties.getProperty("mailAddrList"), ",", true);
            }
            if (properties.containsKey("hostDomain")) {
                hostDomain = properties.getProperty("hostDomain");
            }
            if (properties.containsKey("projectName") && Boolean.FALSE.equals(StringUtils.isEmpty(properties.getProperty("projectName")))) {
                projectName = properties.getProperty("projectName");
            }
            if (properties.containsKey("runner") && Boolean.FALSE.equals(StringUtils.isEmpty(properties.getProperty("runner")))) {
                runner = properties.getProperty("runner");
            }
            if (properties.containsKey("runMode")) {
                runMode = properties.getProperty("runMode");
            }
            if (properties.containsKey("serviceVersion")) {
                serviceVersion = properties.getProperty("serviceVersion");
            }
            if (properties.containsKey("httpHost")) {
                httpHost = properties.getProperty("httpHost");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("配置文件参数非法,请检查test.properties的配置");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("读取test.properties失败");
            e.printStackTrace();
        }
    }

    private static List<String> getListFromString(String from, String regex, boolean... ignoreCase) {
        List<String> list = Lists.newArrayList();
        if (ignoreCase.length != 0 && ignoreCase[0]) {
            from = from.toLowerCase();
        }
        if (from == null || from.length() == 0) {
            return list;
        }
        if (!from.contains(regex)) {
            list.add(from);
            return list;
        }
        list.addAll(Arrays.asList(from.split(regex)));
        return list;
    }

    public static String getServiceVersion() {
        return serviceVersion;
    }

    public static String getProfile() {
        return profile;
    }

    public static String getHttpHost() {
        return httpHost;
    }

    public static void setHttpHost(String httpHost) {
        GlobalConfig.httpHost = httpHost;
    }
}
