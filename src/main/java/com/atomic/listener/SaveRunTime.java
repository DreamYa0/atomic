package com.atomic.listener;

import com.atomic.exception.TestTimeException;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static java.util.stream.Collectors.toList;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 通用测试时间记录工具类
 * @Data 2018/05/30 10:48
 */
public final class SaveRunTime {

    private static List<Long> output = new Vector<>();
    private static Map<Integer, List<Integer>> methodOutputMap = Maps.newHashMap();
    private static ThreadLocal<List<Long>> orphanedOutput = new InheritableThreadLocal<>();

    private SaveRunTime() {
    }

    /**
     * 记录测试方法调用开始时间
     * @param testResult
     */
    public static void startTestTime(ITestResult testResult) {
        time(System.currentTimeMillis(), testResult);
    }

    /**
     * 记录测试方法调用结束时间
     * @param testResult
     */
    public static void endTestTime(ITestResult testResult) {
        time(System.currentTimeMillis(), testResult);
    }

    /**
     * 获取所记录的时间
     * @param tr
     * @return
     */
    public static synchronized List<Long> getOutput(ITestResult tr) {
        List<Long> result = Lists.newArrayList();
        if (tr == null) {
            throw new TestTimeException("获取测试方法调用时间异常！");
        }
        List<Integer> lines = methodOutputMap.get(tr.hashCode());
        if (lines != null) {
            result.addAll(lines.stream().map(n -> getOutput().get(n)).collect(toList()));
        }
        return result;
    }

    /**
     * 清除所记录的时间
     */
    public static void clear() {
        methodOutputMap.clear();
        output.clear();
    }

    private static synchronized void time(Long s, ITestResult m) {
        if (m == null) {
            // 持续输出暂时为Threadlocal字符串列表
            if (orphanedOutput.get() == null) {
                orphanedOutput.set(new ArrayList<>());
            }
            orphanedOutput.get().add(s);
            return;
        }
        int n = getOutput().size();
        List<Integer> lines = methodOutputMap.computeIfAbsent(m.hashCode(), k -> Lists.newArrayList());
        if (orphanedOutput.get() != null) {
            n = n + orphanedOutput.get().size();
            getOutput().addAll(orphanedOutput.get());
            orphanedOutput.remove();
        }
        lines.add(n);
        getOutput().add(s);
    }

    private static List<Long> getOutput() {
        return output;
    }
}
