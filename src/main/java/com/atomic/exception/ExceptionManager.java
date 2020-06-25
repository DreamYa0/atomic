package com.atomic.exception;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.fastjson.JSONException;
import com.atomic.param.Constants;
import com.atomic.param.ParamUtils;
import com.atomic.param.entity.MethodMeta;
import com.atomic.param.entity.MethodMetaUtils;
import com.atomic.tools.report.ParamPrint;
import com.g7.framework.common.dto.Result;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author dreamyao
 * @title
 * @date 2020/6/23 1:15 PM
 * @since 1.0.0
 */
public class ExceptionManager {

    private static final String MSG_SPLIT = " |- ";

    @SuppressWarnings("all")
    public static Result exceptionDeal(Exception exception) {
        String msg;
        if (exception instanceof InvocationTargetException) {
            msg = ExceptionManager.getExceptionProfile(((InvocationTargetException) exception).getTargetException());
        } else {
            msg = ExceptionManager.getExceptionProfile(exception);
        }
        return getResultObj(false, "500", msg);
    }

    @SuppressWarnings("all")
    private static Result getResultObj(Boolean success, String code, String message) {
        Result result = Result.create();
        result.setSuccess(success);
        result.setCode(code);
        result.setDescription(message);
        return result;
    }

    public static void handleException(Exception e,
                                       Map<String, List<Map<String, Object>>> exceptionMsgs,
                                       ITestResult testResult,
                                       Map<String, Object> param) {

        String methodName = testResult.getTestClass().getRealClass().getName();
        Map<String, Object> newParam = ParamUtils.getParamContextWithoutExtraInfo(param);
        String error = String.format("方法执行自动化测试时发生错误！, 方法名是：%s, 入参是：%s",
                methodName, getJSONStringWithDateFormat(newParam));
        Reporter.log(error);

        String errorMsg = e.getMessage() != null ? e.getMessage() :
                ((InvocationTargetException) e).getTargetException() != null ?
                        (((InvocationTargetException) e).getTargetException()).getMessage() : null;

        errorMsg = "(" + methodName + ")" + errorMsg;
        List<Map<String, Object>> exceptions = exceptionMsgs.get(errorMsg);
        if (exceptions == null) {
            exceptions = Lists.newArrayList();
        }
        if (!exceptionMsgs.containsKey(errorMsg)) {
            exceptionMsgs.put(errorMsg, exceptions);
        }
        exceptions.add(newParam);
    }

    public static void printExceptions(Exception exception,
                                       Map<String, List<Map<String, Object>>> exceptionMsgs) throws Exception {
        if (exception != null) {
            System.out.println("------------------- 下面打印所有异常信息：" +
                    " -------------------");
            // 打印所有遇到的异常
            for (String msg : exceptionMsgs.keySet()) {
                System.out.println("------------------- 我是分割线 -------------------");
                System.err.println("异常：" + msg);
                System.err.println("测试用例列表：" + ParamUtils.getJSONString(exceptionMsgs.get(msg)));
            }
            System.out.println("------------------- 异常信息打印完毕，下面抛出异常 " +
                    " -------------------");
            //先判断异常具体类型
            throwNewException(exception);
            // 抛出异常，提醒测试未通过
            throw exception;
        }
    }

    private static String getJSONStringWithDateFormat(Object obj) {
        return ParamUtils.getJSONStringWithDateFormat(obj, true, Constants.DATE_FORMAT);
    }

    public static String getExceptionProfile(Throwable e) {
        // 获取精简过的错误信息，(错误类型+错误描述)，默认截取440个字符（前320个+后120个）
        // 默认440个字符
        return getExceptionProfile(e, null, 440);
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

    public static String getExceptionProfile(Throwable e, String prompt) {
        // 获取精简过的错误信息，(错误类型+错误描述)，默认截取440个字符（前320个+后120个）
        // 默认440个字符
        return getExceptionProfile(e, prompt, 440);
    }

    public static String getExceptionProfile(Throwable e, int errorLen) {
        // 获取精简过的错误信息，(错误类型+错误描述)，默认截取440个字符（前320个+后120个）
        return getExceptionProfile(e, null, errorLen);
    }

    public static String getExceptionProfile(Throwable e, String prompt, int errorLen) {
        // 获取精简过的错误信息，(错误类型+错误描述)，截取 errorLen 个字符。
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

    public static String errorMsgCut(String errorMsg, int maxLen) {
        // 裁剪错误信息，最多只取 maxLen 个字符(maxLen>=200)，规则如下：
        //【只保留前面8/11的字符+后面3/11的字符】 <br>
        // 例如一个数据库的errorMessage长度可达1000个字符，用此方法裁剪后,
        // 假设maxLen=550，那就只保留前400个字符+后150个字符。
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

    public static void handleException(ITestResult iTestResult,
                                       Class<?> clazz,
                                       String testMethodName,
                                       Map<String, Object> param,
                                       Exception e,
                                       CompletableFuture<Object> future) {

        //beforeTest里面的异常也直接抛出
        if (isExceptionThrowsBySpecialMethod(e, "beforeTest")) {
            Reporter.log("beforeTest方法执行异常！");
            throwNewException(e);
            throw new RuntimeException(e);
        }

        // 期望结果为 Y 直接抛异常
        if (ParamUtils.isExpectSuccess(param)) {
            throwNewException(e);
            throw new RuntimeException(e);
        }

        param.put(Constants.RESULT_NAME, exceptionDeal(e));
        if (future != null && clazz != null && testMethodName != null) {
            try {
                MethodMeta methodMeta = MethodMetaUtils.getMethodMeta(iTestResult, clazz, testMethodName,
                        param, future);
                ParamPrint.resultPrint(methodMeta.getInterfaceMethod().getName(),
                        param.get(Constants.RESULT_NAME), param, param.get(Constants.PARAMETER_NAME_));
            } catch (Exception e1) {
                Reporter.log("获取测试所需的属性时异常！");
                throw new MethodMetaException(e1);
            }
        }
        throwNewException(e);
        throw new RuntimeException(e);
    }

    public static void throwNewException(Exception e) {
        // 判断异常具体类型,方便数据展示平台统计
        if (e instanceof NullPointerException) {
            throw new NullPointerException(e.getMessage());
        } else if (e instanceof AnnotationException) {
            throw new AnnotationException(e);
        } else if (e instanceof ClassCastException) {
            throw new ClassCastException(e.getMessage());
        } else if (e instanceof ArrayIndexOutOfBoundsException) {
            throw new ArrayIndexOutOfBoundsException(e.getMessage());
        } else if (e instanceof JSONException) {
            throw new JSONException(e.getMessage());
        } else if (e instanceof NumberFormatException) {
            throw new NumberFormatException(e.getMessage());
        } else if (e instanceof ArrayStoreException) {
            throw new ArrayStoreException(e.getMessage());
        } else if (e instanceof RpcException) {
            throw new RpcException(e);
        } else if (e instanceof GenericException) {
            throw new GenericException(e);
        } else if (e instanceof AssertJDBException) {
            throw new AssertJDBException(e);
        } else if (e instanceof DubboServiceException) {
            throw new DubboServiceException(e);
        } else if (e instanceof AutoTestException) {
            throw new AutoTestException(e);
        } else if (e instanceof InjectResultException) {
            throw new InjectResultException(e);
        } else if (e instanceof IllegalStateException) {
            throw new IllegalStateException(e);
        } else if (e instanceof QueryDataException) {
            throw new QueryDataException(e);
        } else if (e instanceof TestTimeException) {
            throw new TestTimeException(e);
        } else if (e instanceof InvokeException) {
            throw new InvokeException(e);
        } else if (e instanceof GetBeanException) {
            throw new GetBeanException(e);
        } else if (e instanceof RollBackException) {
            throw new RollBackException(e);
        } else if (e instanceof ParameterException) {
            throw new ParameterException(e);
        } else {
            throw new RuntimeException(e);
        }
    }
}
