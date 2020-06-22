package com.atomic.runner;

import com.atomic.param.Constants;
import com.atomic.util.FileUtils;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2017/9/11 22:41
 */
public class MethodSelectorConfig {

    public static final MethodSelectorConfig INSTANCE = new MethodSelectorConfig();
    private static final String testConfFilePath = "/test.properties";
    private List<String> packageList = Lists.newArrayList();
    private List<String> groupList = Lists.newArrayList();
    private List<String> classList = Lists.newArrayList();

    private MethodSelectorConfig() {
        String packages;
        String groups;
        String methods;
        InputStream in = FileUtils.getTestFileInputStream(testConfFilePath);
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (properties.containsKey(Constants.RUN_TEST_PACKAGES)) {
            packages = properties.getProperty(Constants.RUN_TEST_PACKAGES);
            if (packages != null && !"".equals(packages)) {
                packageList.addAll(Arrays.asList(packages.split(",")));
            }
        }
        if (properties.containsKey(Constants.RUN_TEST_GROUPS)) {
            groups = properties.getProperty(Constants.RUN_TEST_GROUPS);
            if (groups != null && !"".equals(groups)) {
                groupList.addAll(Arrays.asList(groups.split(",")));
            }
        }
        if (properties.containsKey(Constants.RUN_TEST_CLASSES)) {
            methods = properties.getProperty(Constants.RUN_TEST_CLASSES);
            if (methods != null && !"".equals(methods)) {
                classList.addAll(Arrays.asList(methods.split(",")));
            }
        }
    }

    public static MethodSelectorConfig getInstance() {
        return INSTANCE;
    }

    public List<String> getPackageList() {
        return packageList;
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public List<String> getClassList() {
        return classList;
    }
}
