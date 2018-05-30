package com.atomic.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static Map<String, String> getMapFromFile(File f) {
        Map<String, String> maps = null;
        try {
            maps = getMapFromStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return maps;
    }

    private static Map<String, String> getMapFromStream(InputStream in) {
        BufferedReader br = null;
        Map<String, String> maps = null;
        try {
            maps = new HashMap<>();
            br = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.contains("=")) {
                    String[] ss = line.split("=");
                    String key = ss[0] == null ? "" : ss[0].trim();
                    String value = ss[1] == null ? "" : ss[1].trim();
                    maps.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return maps;
    }

    /**
     * 用于获取打包中的resources文件
     * @param filePath
     * @return
     */
    public static InputStream getFileInputStream(String filePath) {
        try {
            ClassLoader loader = FileUtils.class.getClassLoader();
            return loader.getResourceAsStream(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用于获取执行时的resources文件(可读取test/resources/下的文件,相对路径)
     * @param filePath
     * @return
     */
    public static InputStream getTestFileInputStream(String filePath) {
        try {
            return FileUtils.class.getResourceAsStream(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    /**
     * 普通写文件
     * @param genFileDir  文件夹
     * @param genFileName 文件名
     * @param context     内容
     * @throws IOException
     */
    public static void writeFile(String genFileDir, String genFileName, String context) throws IOException {
        File genDir = new File(genFileDir);
        //删除存在目录
        genDir.deleteOnExit();
        genDir.mkdirs();
        writeFile(genFileDir + genFileName, context);
    }

    /**
     * 普通写文件
     * @param genFileName 文件名
     * @param context     内容
     * @throws IOException
     */
    private static void writeFile(String genFileName, String context) throws IOException {
        FileWriter writer = new FileWriter(genFileName);
        writer.write(context);
        writer.close();
    }

    /**
     * 移动 文件或者文件夹
     * @param oldPath
     * @param newPath
     * @throws IOException
     */
    public static void moveTo(String oldPath, String newPath) throws IOException {
        copyFile(oldPath, newPath);
        deleteFile(oldPath);
    }

    /**
     * 删除 文件或者文件夹
     * @param filePath
     */
    private static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (File f : list) {
                deleteFile(f.getAbsolutePath());
            }
        }
        file.delete();
    }

    /**
     * 复制 文件或者文件夹
     * @param oldPath
     * @param newPath
     * @throws IOException
     */
    private static void copyFile(String oldPath, String newPath) throws IOException {
        System.out.println("copy file from [" + oldPath + "] to [" + newPath + "]");
        File oldFile = new File(oldPath);
        if (oldFile.exists()) {
            if (oldFile.isDirectory()) { // 如果是文件夹
                File newPathDir = new File(newPath);
                newPathDir.mkdirs();
                File[] lists = oldFile.listFiles();
                if (lists != null && lists.length > 0) {
                    for (File file : lists) {
                        copyFile(file.getAbsolutePath(), newPath.endsWith(File.separator) ? newPath + file.getName() : newPath + File.separator + file.getName());
                    }
                }
            } else {
                InputStream inStream = new FileInputStream(oldFile);  //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                write2Out(inStream, fs);
                inStream.close();
            }
        }
    }

    /**
     * 重命名文件
     * @param file
     * @param name
     * @return
     */
    public static File renameFile(File file, String name) {
        String fileName = file.getParent() + File.separator + name;
        File dest = new File(fileName);
        file.renameTo(dest);
        return dest;
    }

    /**
     * 压缩多个文件。
     * @param zipFileName 压缩输出文件名
     * @param files       需要压缩的文件
     * @return
     * @throws Exception
     */
    public static File createZip(String zipFileName, File... files) throws Exception {
        File outFile = new File(zipFileName);
        ZipOutputStream out = null;
        BufferedOutputStream bo = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(outFile));
            bo = new BufferedOutputStream(out);
            for (File file : files) {
                zip(out, file, file.getName(), bo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bo.close();
            } finally {
                out.close(); // 输出流关闭
            }
        }
        return outFile;
    }

    /**
     * @param zipFileName 压缩输出文件名
     * @param inputFile   需要压缩的文件
     * @return
     * @throws Exception
     */
    public static File createZip(String zipFileName, File inputFile) throws Exception {
        File outFile = new File(zipFileName);
        ZipOutputStream out = null;
        BufferedOutputStream bo = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(outFile));
            bo = new BufferedOutputStream(out);
            zip(out, inputFile, inputFile.getName(), bo);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bo.close();
            } finally {
                out.close(); // 输出流关闭

            }
        }
        return outFile;
    }

    private static void zip(ZipOutputStream out, File f, String base, BufferedOutputStream bo) throws Exception { // 方法重载
        if (f.isDirectory()) {
            File[] fl = f.listFiles();
            if (fl == null || fl.length == 0) {
                out.putNextEntry(new ZipEntry(base + "/")); // 创建创建一个空的文件夹
            } else {
                for (int i = 0; i < fl.length; i++) {
                    zip(out, fl[i], base + "/" + fl[i].getName(), bo); // 递归遍历子文件夹

                }
            }

        } else {
            out.putNextEntry(new ZipEntry(base)); // 创建zip压缩进入 base 文件
            System.out.println(base);
            BufferedInputStream bi = new BufferedInputStream(new FileInputStream(f));
            try {
                write2Out(bi, out);
            } catch (IOException e) {
                //Ignore
            } finally {
                bi.close();// 输入流关闭
            }
        }
    }

    private static void write2Out(InputStream input, OutputStream out) throws IOException {
        byte[] b = new byte[1024];
        int c = 0;
        while ((c = input.read(b)) != -1) {
            out.write(b, 0, c);
            out.flush();
        }
        out.flush();
    }
}
