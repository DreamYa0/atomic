package com.atomic.param.excel.parser;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;

public class YamlResolver {
    /**
     * 从yaml文件读取内容
     * @param file
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCaseListFromYaml(File file) {
        Map<String, Object> yamlMap = null;

        // 读取yaml文件内容
        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file.getAbsolutePath());
            yamlMap = yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 提取并返回测试用例数据
        if (yamlMap.containsKey("dubboCaseList")) {
            return (List<Map<String, Object>>)yamlMap.get("dubboCaseList");
        } else if (yamlMap.containsKey("httpCaseList")) {
            return (List<Map<String, Object>>)yamlMap.get("httpCaseList");
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        YamlResolver yamlResolver = new YamlResolver();
        String filePath = "/Users/yangminhan/Documents/codeSave/workspace_java/atomic/target/test-classes/com/atomic/autotest/payservice/TestAccountTakeOff.yaml";
        File file = new File(filePath);
        System.out.println(yamlResolver.getCaseListFromYaml(file));
    }
}
