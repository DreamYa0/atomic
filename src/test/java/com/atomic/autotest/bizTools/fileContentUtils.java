package com.atomic.autotest.bizTools;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;

public class fileContentUtils {
    public static String getFileContent(String filePath) {
        String inParamsPath = "/src/test/resources/com/atomic/autotest/dataTemplates/";
        String workDir = System.getProperty("user.dir");
//        System.out.println(workDir);

        StringBuffer content = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(workDir + inParamsPath + filePath)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return content.toString().replaceAll("  ","");
    }

    public static JsonObject getJsonFromFile(String filePath) {
        String jsonStr = getFileContent(filePath);
//        System.out.println(jsonStr);

        JsonObject jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();

        return jsonObject;
    }

    public static void main(String[] args) {
//        String basePath= TestAccountTakeOff.class.getResource("/").getPath();
        System.out.println(getFileContent("payService/AccountTakeOff.json"));
        System.out.println(getJsonFromFile("payService/AccountTakeOff.json"));
    }
}
