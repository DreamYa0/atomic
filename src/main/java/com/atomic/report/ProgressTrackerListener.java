package com.atomic.report;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.Calendar;

/**
 * 通用测试运行过程统计监听器
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/7/11.
 */
public class ProgressTrackerListener extends TestListenerAdapter implements IInvokedMethodListener {

    public static int totalRun = 0;
    private long startTime = 0;
    private int totalExecuted = 0;

    @Override
    public void onStart(ITestContext testContext) {
        totalRun = testContext.getAllTestMethods().length;
    }

    @Override
    public void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
        if (invokedMethod.isTestMethod()) {
            long elapsedTime = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
            int remainingTestCount = totalRun - totalExecuted;
            long remainingTime = (elapsedTime / totalExecuted) * remainingTestCount;
            System.out.println("[Progress：]" + formPercentageStr(totalExecuted, totalRun) + " (" + totalExecuted + "/" + totalRun + ") " + ", 测试运行时间:"
                    + formTimeStr(elapsedTime) + ", 测试预计剩余时间:" + formTimeStr(remainingTime));
        }
    }

    @Override
    public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult arg1) {
        if (invokedMethod.isTestMethod()) {
            if (startTime == 0) {
                startTime = Calendar.getInstance().getTimeInMillis();
            }
            totalExecuted += 1;
        }
    }

    private String formPercentageStr(long executedTestCount, long totalTestCount) {
        return Math.round((double) executedTestCount * 100 / (double) totalTestCount) + "%";
    }

    private String formTimeStr(long valueInSeconds) {
        long hours = valueInSeconds / 3600;
        valueInSeconds = valueInSeconds % 3600;
        long minutes = valueInSeconds / 60;
        valueInSeconds = valueInSeconds % 60;
        return toTwoDigitsStr(hours) + ":" + toTwoDigitsStr(minutes) + ":" + toTwoDigitsStr(valueInSeconds);
    }

    private String toTwoDigitsStr(long value) {
        if (value < 10) {
            return "0" + value;
        } else {
            return String.valueOf(value);
        }
    }
}
