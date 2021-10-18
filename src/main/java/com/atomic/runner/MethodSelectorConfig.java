package com.atomic.runner;

import com.atomic.config.ConfigConstants;
import com.atomic.config.AtomicConfig;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2017/9/11 22:41
 */
public class MethodSelectorConfig {

    public static final MethodSelectorConfig INSTANCE = new MethodSelectorConfig();
    private final List<String> packageList = Lists.newArrayList();
    private final List<String> groupList = Lists.newArrayList();
    private final List<String> classList = Lists.newArrayList();

    private MethodSelectorConfig() {
        String packages;
        String groups;
        String methods;
        if (AtomicConfig.containsKey(ConfigConstants.RUN_TEST_PACKAGES)) {
            packages = AtomicConfig.getStr(ConfigConstants.RUN_TEST_PACKAGES);
            if (packages != null && !"".equals(packages)) {
                packageList.addAll(Arrays.asList(packages.split(",")));
            }
        }
        if (AtomicConfig.containsKey(ConfigConstants.RUN_TEST_GROUPS)) {
            groups = AtomicConfig.getStr(ConfigConstants.RUN_TEST_GROUPS);
            if (groups != null && !"".equals(groups)) {
                groupList.addAll(Arrays.asList(groups.split(",")));
            }
        }
        if (AtomicConfig.containsKey(ConfigConstants.RUN_TEST_CLASSES)) {
            methods = AtomicConfig.getStr(ConfigConstants.RUN_TEST_CLASSES);
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
