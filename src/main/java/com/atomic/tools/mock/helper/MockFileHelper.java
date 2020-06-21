package com.atomic.tools.mock.helper;


import com.atomic.tools.mock.data.MockContext;

import java.io.File;

public class MockFileHelper {

    //获取mock数据路径
    public static String getMockFile(int caseIndex) {
        String testPath = getTestResourcesPath(MockContext.getContext().getTestClass());
        return testPath + MockContext.getContext().getTestClass().replace(".", File.separator) +
                "_" + caseIndex + ".mock";
    }

    private static String getTestResourcesPath(String className) {
        if (className == null) {
            return "";
        }
        String basePath = getBasePath(className);
        return basePath + "src" + File.separator
                + "test" + File.separator
                + "resources" + File.separator;
    }

    public static String getTestClassPath(String className) {
        if (className == null) {
            return "";
        }
        String basePath = getBasePath(className);
        return basePath + "src" + File.separator
                + "test" + File.separator
                + "java" + File.separator;
    }

    private static String getBasePath(String className) {
        if (className == null) {
            return "";
        }
        try {
            Class<?> clazz = Class.forName(className);
            String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
            int index = path.indexOf("target");
            path = path.substring(0, index);
            return path;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
