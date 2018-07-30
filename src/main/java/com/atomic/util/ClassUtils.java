package com.atomic.util;

import org.assertj.core.util.Lists;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类相关的工具类
 * @modify dreamyao
 * mail:chenlaing@zbj.com
 */
public final class ClassUtils {

    private ClassUtils() {
    }

    /**
     * 取得某个接口下所有实现这个接口的类
     */
    @SuppressWarnings("unchecked")
    public static List<Class> getAllClassByInterface(Class c) {
        List<Class> returnClassList = null;
        if (c.isInterface()) {
            // 获取当前的包名
            String packageName = c.getPackage().getName();
            // 获取当前包下以及子包下所以的类
            List<Class<?>> allClass = getClasses(packageName);
            if (allClass != null) {
                returnClassList = new ArrayList<Class>();
                for (Class classes : allClass) {
                    // 判断是否是同一个接口
                    if (c.isAssignableFrom(classes)) {
                        // 本身不加入进去
                        if (!c.equals(classes)) {
                            returnClassList.add(classes);
                        }
                    }
                }
            }
        }
        return returnClassList;
    }

    /**
     * 取得某一类所在包的所有类名 不含迭代
     * @param classLocation
     * @param packageName
     * @return
     */
    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        //将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packeageDir = new File(realClassLocation);
        if (packeageDir.isDirectory()) {
            return packeageDir.list();
        }
        return null;
    }

    /**
     * 从包package中获取所有的Class
     * @param packageName
     * @return
     */
    public static List<Class<?>> getClasses(String packageName) {
        //第一个class类的集合
        List<Class<?>> classes = Lists.newArrayList();
        //是否循环迭代
        //获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        //定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            //循环迭代下去
            while (dirs.hasMoreElements()) {
                //获取下一个元素
                URL url = dirs.nextElement();
                //得到协议的名称
                String protocol = url.getProtocol();
                //如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    //获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, true, classes);
                } else if ("jar".equals(protocol)) {
                    //如果是jar包文件
                    //定义一个JarFile
                    JarFile jar;
                    try {
                        //获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        //从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        //同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            //如果是以/开头的
                            if (name.charAt(0) == '/') {
                                //获取后面的字符串
                                name = name.substring(1);
                            }
                            //如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                //如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    //获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                //如果可以迭代下去 并且是一个包
                                //如果是一个.class文件 而且不是目录
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    //去掉后面的".class" 获取真正的类名
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                                    try {
                                        //添加到classes
                                        classes.add(Class.forName(packageName + '.' + className));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) {
        //获取此包的目录 建立一个File
        File dir = new File(packagePath);
        //如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //如果存在 就获取包下的所有文件 包括目录
        //自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirfiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        //循环所有文件
        if (dirfiles != null) {
            for (File file : dirfiles) {
                //如果是目录 则继续扫描
                if (file.isDirectory()) {
                    findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
                } else {
                    //如果是java类文件 去掉后面的.class 只留下类名
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        //添加到集合中去
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class ReflectUtilSecurityManager extends SecurityManager {
        public Class getCallerClass(final int callStackDepth) {
            return getClassContext()[callStackDepth + 1];
        }
    }

    private static ClassUtils.ReflectUtilSecurityManager SECURITY_MANAGER;

    static {
        try {
            SECURITY_MANAGER = new ClassUtils.ReflectUtilSecurityManager();
        } catch (Exception ex) {
            SECURITY_MANAGER = null;
        }
    }

    /**
     * Emulates <code>Reflection.getCallerClass</code> using standard API.
     * This implementation uses custom <code>SecurityManager</code>
     * and it is the fastest. Other implementations are:
     * <ul>
     * <li><code>new Throwable().getStackTrace()[callStackDepth]</code></li>
     * <li><code>Thread.currentThread().getStackTrace()[callStackDepth]</code> (the slowest)</li>
     * </ul>
     * <p>
     * In case when usage of <code>SecurityManager</code> is not allowed,
     * this method fails back to the second implementation.
     * <p>
     * Note that original <code>Reflection.getCallerClass</code> is way faster
     * then any emulation.
     */
    public static Class getCallerClass(int framesToSkip) {
        if (SECURITY_MANAGER != null) {
            return SECURITY_MANAGER.getCallerClass(framesToSkip);
        }

        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();

        if (framesToSkip >= 2) {
            framesToSkip += 4;
        }

        String className = stackTraceElements[framesToSkip].getClassName();

        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException cnfex) {
            throw new UnsupportedOperationException(className + " not found.");
        }
    }
}