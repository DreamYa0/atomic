package com.atomic.tools.mock.helper;


import com.alibaba.fastjson.JSON;
import com.atomic.tools.mock.dto.MockData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.atomic.tools.mock.data.MockContext.getContext;

public class MockFileHelper {

    /**
     * 保存Mock数据
     */
    public static void loadData() {
        try {
            File file = new File(getMockFile(getContext().getCaseIndex()));
            String data = getFileString(file);
            MockData mockData = JSON.parseObject(data, MockData.class);
            getContext().setMockData(mockData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除Mock数据
     */
    public static void deleteData() {
        removeDir(getMockFile(getContext().getCaseIndex()));
    }

    /**
     * 功 能: 删除文件夹 参 数: strDir 要删除的文件夹名称 返回值: 如果成功true;否则false
     * @param strDir
     * @return
     */
    public static boolean removeDir(String strDir) {
        File rmDir = new File(strDir);
        if (rmDir.isDirectory() && rmDir.exists()) {
            String[] fileList = rmDir.list();
            assert fileList != null;
            for (int i = 0; i < fileList.length; i++) {
                String subFile = strDir + File.separator + fileList[i];
                File tmp = new File(subFile);
                if (tmp.isFile()) {
                    tmp.delete();
                }
                if (tmp.isDirectory()) {
                    removeDir(subFile);
                }
            }
            rmDir.delete();
        } else {
            return false;
        }
        return true;
    }

    //获取mock数据路径
    public static String getMockFile(int caseIndex) {
        String testPath = getTestResourcesPath(getContext().getTestClass());
        return testPath + getContext().getTestClass().replace(".", File.separator) +
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
