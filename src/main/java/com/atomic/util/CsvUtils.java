package com.atomic.util;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.Lists;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 通用CSV文件处理工具类
 * @author dreamyao
 * @version 1.0
 * @Data 2018/05/30 10:48
 */
public final class CsvUtils {

    private CsvUtils() {
    }

    /**
     * CSV文件读取
     * @param file
     * @return
     */
    public static List<Map<String, Object>> handleFile(File file) {
        return handleFile(file, ';');
    }

    /**
     * CSV文件按符号读取读取
     * @param file
     * @param fieldSeperator
     * @return
     */
    private static List<Map<String, Object>> handleFile(File file, char fieldSeperator) {
        List<Map<String, Object>> datas = Lists.newArrayList();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            CSVReader creader = new CSVReader(reader, fieldSeperator);
            List<String[]> dataList = creader.readAll();
            datas = ExcelUtils.generateCasesFromCsv(dataList);
            creader.close();
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return datas;
    }

    /**
     * 生成http请求的CSV文件内容
     * @param csvFile
     */
    public static void handleHttpFile(File csvFile) {
        BufferedWriter csvFileOutputStream = null;
        try {
            // UTF-8使正确读取分隔符","
            csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"));
            // 写入文件内容
            csvFileOutputStream.write(new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}));
            csvFileOutputStream.write("caseName,用例标题\n");
            csvFileOutputStream.write("httpMode,\n");
            csvFileOutputStream.write("httpHost,\n");
            csvFileOutputStream.write("httpMethod,\n");
            csvFileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (csvFileOutputStream != null) {
                    csvFileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
