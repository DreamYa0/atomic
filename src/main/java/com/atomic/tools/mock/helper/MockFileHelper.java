package com.atomic.tools.mock.helper;


import com.atomic.tools.mock.data.MockContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MockFileHelper {

    //获取mock数据路径
    public static String getMockFile(int caseIndex) {
        String testPath = getTestResourcesPath(MockContext.getContext().getTestClass());
        return testPath + MockContext.getContext().getTestClass().replace(".", File.separator) +
                "_" + caseIndex + ".mock";
    }

    public static String getFileString(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                stringBuilder.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    //
                }
            }
        }
        return stringBuilder.toString();
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
