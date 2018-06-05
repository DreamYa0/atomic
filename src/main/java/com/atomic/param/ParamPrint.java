package com.atomic.param;

import io.restassured.response.Response;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 通用出入参数打印工具类
 * @author dreamyao
 * @version 1.0
 */
public final class ParamPrint {

    private ParamPrint() {
    }

    /**
     * Dubbo入参出参打印
     * @param method     测试方法名称
     * @param result     接口返回结果对象
     * @param map        入参上下文
     * @param parameters 接口入参
     */
    public static void resultPrint(String method, Object result, Map<String, Object> map, Object... parameters) {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            System.out.println("------------------" + method + " request------------------------");
            System.out.println("入参：" + ParamUtils.getJSONStringWithDateFormat(parameters, true, Constants.DATE_FORMAT));
            if (map.get(Constants.CASE_NAME) == null) {
                if ((ParamUtils.isAutoTest(map))) {
                    System.out.println("------------------" + method + " 自动化测试 (" + map.get(Constants.EXCEL_DESC) + ")------------------------");
                } else {
                    System.out.println("------------------" + method + " 期望 (" + map.get(Constants.ASSERT_RESULT) + ")(" + map.get(Constants.EXCEL_DESC) + ")------------------------");
                }
            } else {
                if ((ParamUtils.isAutoTest(map))) {
                    System.out.println("------------------" + method + " 自动化测试 (" + map.get(Constants.CASE_NAME) + ")------------------------");
                } else {
                    System.out.println("------------------" + method + " 期望 (" + map.get(Constants.ASSERT_RESULT) + ")(" + map.get(Constants.CASE_NAME) + ")------------------------");
                }
            }
            System.out.println("出参：" + ParamUtils.getJSONStringWithDateFormat(result, true, Constants.DATE_FORMAT));
            System.out.println("------------------------------------------------");
            System.out.println("");
        } finally {
            lock.unlock();
        }
    }

    /**
     * REST 接口出参入参打印
     * @param method     测试方法名称
     * @param response   接口返回结果对象
     * @param map        入参上下文
     * @param parameters 接口入参
     */
    @SuppressWarnings("unchecked")
    public static void resultPrint(String method, Response response, Map<String, Object> map, Object... parameters) {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            System.out.println("------------------" + method + " request------------------------");

            Map<String, Object> param = (Map<String, Object>) parameters[0];
            if (Boolean.FALSE.equals(CollectionUtils.isEmpty(param)) && param.containsKey("request")) {
                Object paramJsonValue = param.get("request");
                System.out.println("入参：\n" + formatJson(paramJsonValue.toString()));
            } else {
                System.out.println("入参：" + ParamUtils.getJSONStringWithDateFormat(parameters, true, Constants.DATE_FORMAT));
            }

            if (map.get(Constants.CASE_NAME) == null) {
                System.out.println("------------------" + method + " 期望 (" + map.get(Constants.ASSERT_RESULT) + ")(" + map.get(Constants.EXCEL_DESC) + ")------------------------");
            } else {
                System.out.println("------------------" + method + " 期望 (" + map.get(Constants.ASSERT_RESULT) + ")(" + map.get(Constants.CASE_NAME) + ")------------------------");
            }
            System.out.println("出参：");
            response.body().prettyPrint();
            System.out.println("------------------------------------------------");
            System.out.println("");
        } finally {
            lock.unlock();
        }
    }

    /**
     * HTTP 接口出参入参打印
     * @param method     测试方法名称
     * @param result     接口返回结果对象
     * @param map        入参上下文
     * @param parameters 接口入参
     */
    @SuppressWarnings("unchecked")
    public static void resultPrint(String method, String result, Map<String, Object> map, Object... parameters) {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            System.out.println("------------------" + method + " request------------------------");

            Map<String, Object> param = (Map<String, Object>) parameters[0];
            if (Boolean.FALSE.equals(CollectionUtils.isEmpty(param)) && param.containsKey("request")) {
                Object paramJsonValue = param.get("request");
                System.out.println("入参：\n" + formatJson(paramJsonValue.toString()));
            } else {
                System.out.println("入参：" + ParamUtils.getJSONStringWithDateFormat(parameters, true, Constants.DATE_FORMAT));
            }

            if (map.get(Constants.CASE_NAME) == null) {
                System.out.println("------------------" + method + " 期望 (" + map.get(Constants.ASSERT_RESULT) + ")(" + map.get(Constants.EXCEL_DESC) + ")------------------------");
            } else {
                System.out.println("------------------" + method + " 期望 (" + map.get(Constants.ASSERT_RESULT) + ")(" + map.get(Constants.CASE_NAME) + ")------------------------");
            }
            System.out.println("出参：\n" + formatJson(result));
            System.out.println("------------------------------------------------");
            System.out.println("");
        } finally {
            lock.unlock();
        }
    }

    private static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        char last;
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        // sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        // sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        // sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }
        return sb.toString();
    }

    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }
}
