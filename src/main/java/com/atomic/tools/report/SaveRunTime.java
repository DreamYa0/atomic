package com.atomic.tools.report;

import com.atomic.exception.TestTimeException;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.stream.Collectors.toList;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 通用测试时间记录工具类
 * @date 2018/05/30 10:48
 */
public final class SaveRunTime {

    private static List<Long> output = new CopyOnWriteArrayList<>();
    private static ConcurrentMap<Integer, List<Integer>> methodOutputMap = new ConcurrentHashMap<>(32);
    private static ThreadLocal<List<Long>> orphanedOutput = new InheritableThreadLocal<>();

    private SaveRunTime() {
    }

    public static void startTestTime(ITestResult testResult) {
        // 记录测试方法调用开始时间
        time(System.currentTimeMillis(), testResult);
    }

    public static void endTestTime(ITestResult testResult) {
        // 记录测试方法调用结束时间
        time(System.currentTimeMillis(), testResult);
    }

    public static synchronized List<Long> getOutput(ITestResult tr) {
        // 获取所记录的时间
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

    public static void clear() {
        // 清除所记录的时间
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
