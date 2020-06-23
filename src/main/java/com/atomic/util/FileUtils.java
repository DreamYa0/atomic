package com.atomic.util;

import java.io.InputStream;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public final class FileUtils {

    private FileUtils() {

    }

    public static InputStream getTestFileInputStream(String filePath) {
        // 用于获取执行时的resources文件(可读取test/resources/下的文件,相对路径)
        try {
            return FileUtils.class.getResourceAsStream(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
