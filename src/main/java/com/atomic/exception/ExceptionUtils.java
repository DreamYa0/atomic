package com.atomic.exception;


import com.atomic.param.*;
import com.atomic.param.entity.MethodMeta;
import org.apache.commons.lang3.StringUtils;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 设计目的：<br>
 * 1、有些错误信息(errorMsg)非常的长，比如Spring或Hibernate或JDBC的错误信息;<br>
 * 2、一些异常的堆栈特别大，动辄几十、上百行，例如Tomcat、WebSphere的异常信息。<br>
 * 为了精简错误信息，以及防止大量冗余信息记录到日志中，故设计了这个方案：截取核心的错误信息——详见下面的算法。
 * @author zollty
 * @since 2013-6-27
 */
public abstract class ExceptionUtils {

    private static final String MSG_SPLIT = " |- ";

    /**
     * 获取精简过的错误信息，(错误类型+错误描述)，默认截取440个字符（前320个+后120个）
     * @param e
     * @return
     */
    public static String getExceptionProfile(Throwable e) {
        return getExceptionProfile(e, null, 440); // 默认440个字符
    }

    public static boolean isExceptionThrowsBySpecialMethod(Exception e, String methodName) {
        StackTraceElement[] st = e.getStackTrace();
        for (StackTraceElement stackTraceElement : st) {
            if (methodName.equals(stackTraceElement.getMethodName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取精简过的错误信息，(错误类型+错误描述)，默认截取440个字符（前320个+后120个）
     * @param prompt 附加提示，可为 null
     */
    public static String getExceptionProfile(Throwable e, String prompt) {
        return getExceptionProfile(e, prompt, 440); // 默认440个字符
    }

    /**
     * 获取精简过的错误信息，(错误类型+错误描述)，默认截取440个字符（前320个+后120个）
     * @param errorLen 截取错误字符串的最大长度，比如 500
     */
    public static String getExceptionProfile(Throwable e, int errorLen) {
        return getExceptionProfile(e, null, errorLen);
    }

    /**
     * 获取精简过的错误信息，(错误类型+错误描述)，截取 errorLen 个字符。
     * @param prompt   附加提示，可为 null
     * @param errorLen 截取错误字符串的最大长度，比如 500
     */
    public static String getExceptionProfile(Throwable e, String prompt, int errorLen) {
        // 如果error过长,则精简到errorLen个字符
        String msg = e.toString();
        if (e.getMessage() != null) {
            msg += e.getMessage().replace(e.toString(), "");
        }
        String error = errorMsgCut(msg, errorLen);
        // getErrorDescription
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(prompt)) {
            sb.append(prompt);
            sb.append(MSG_SPLIT);
        }
        sb.append(error);

        return sb.toString();
    }

    /**
     * 裁剪错误信息，最多只取 maxLen 个字符(maxLen>=200)，规则如下： <br>
     * 【只保留前面8/11的字符+后面3/11的字符】 <br>
     * 例如一个数据库的errorMessage长度可达1000个字符，用此方法裁剪后, <br>
     * 假设maxLen=550，那就只保留前400个字符+后150个字符。
     * @param maxLen 截取错误字符串的最大长度，比如500
     * @return 精简后的错误信息字符串
     * @author zollty 2013-7-27
     */
    public static String errorMsgCut(String errorMsg, int maxLen) {
        if (errorMsg == null) {
            return null;
        }
        int strLen = errorMsg.length();
        if (strLen < 200 || strLen < maxLen) { // 小于200的字符，不做处理
            return errorMsg;
        }
        int front = maxLen * 8 / 11;
        errorMsg = errorMsg.substring(0, front) + "......" + errorMsg.substring(strLen - maxLen + front, strLen);
        return errorMsg;
    }

    /**
     * 异常处理
     * @param clazz
     * @param testMethodName
     * @param e
     * @param param
     */
    public static void handleException(ITestResult iTestResult, Class<?> clazz, String testMethodName, Map<String, Object> param, Exception e, CompletableFuture<Object> future) {
        //beforeTest里面的异常也直接抛出
        if (isExceptionThrowsBySpecialMethod(e, "beforeTest")) {
            Reporter.log("beforeTest方法执行异常！");
            ThrowException.throwNewException(e);
            throw new RuntimeException(e);
        }
        // 期望结果为 Y 直接抛异常
        if (ParamUtils.isExpectSuccess(param)) {
            ThrowException.throwNewException(e);
            throw new RuntimeException(e);
        }
        param.put(Constants.RESULT_NAME, ResultAssert.exceptionDeal(e));
        if (future != null && clazz != null && testMethodName != null) {
            try {
                MethodMeta methodMeta = MethodMetaUtils.getMethodMeta(iTestResult, clazz, testMethodName, param, future);
                ParamPrint.resultPrint(methodMeta.getInterfaceMethod().getName(), param.get(Constants.RESULT_NAME), param, param.get(Constants.PARAMETER_NAME_));
            } catch (Exception e1) {
                Reporter.log("获取测试所需的属性时异常！");
                throw new MethodMetaException(e1);
            }
        }
        ThrowException.throwNewException(e);
        throw new RuntimeException(e);
    }

    public static interface LineChecker {
        boolean checkLine(String line);
    }
}
