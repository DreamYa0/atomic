package com.atomic.tools.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atomic.tools.dubbo.DubboRegisterService;
import com.atomic.tools.zookeeper.ZkConsole;
import com.atomic.util.HttpClientUtils;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

final class HttpInvokeConsole {

    private ZkConsole zkConsole;
    private HttpClientUtils client;

    public HttpInvokeConsole(ZkConsole zkConsole) {
        this.zkConsole = zkConsole;
        client = HttpClientUtils.createZBJHttpClient();
    }

    private static <T> T getObject(Class<?> serviceClass, String methodName, Object request, String jsonString) {
        if (jsonString == null || jsonString.length() == 0) {
            return null;
        }
        try {
            java.lang.reflect.Method m = serviceClass.getMethod(methodName, request.getClass());
            Type t = m.getGenericReturnType();
            return getTObject(jsonString, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * JSONString 转化成对应的类
     */
    private static <T> T getTObject(String jsonString, Type type) {
        try {
            return JSON.parseObject(jsonString, type);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getUrl(DubboRegisterService dubboService, String methodName) {
        if (dubboService == null) {
            System.out.println("don't find the service");
            return null;
        }
        if (!dubboService.getMethods().contains(methodName)) {
            System.out.println("the service(" + dubboService.getServiceName() + ") don't has this method(" + methodName + ")");
            return null;
        }
        return "http://" + dubboService.getPostDomain() + ":" + dubboService.getPostPort()
                + "/api/" + dubboService.getServiceName()
                + "/" + methodName;
    }

    String invoke(String serviceName, String methodName, Object request) {
        DubboRegisterService dubboService = zkConsole.getDubboService(serviceName);
        String url = getUrl(dubboService, methodName);
        if (url == null) {
            return null;
        }
        String value = JSON.toJSONString(request);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("param", value);
        System.out.println("url: " + url);
        System.out.println("params: " + value);
        String re = client.doPost(url, params).toResult().get(0);
        System.out.println("re: " + re);
        return re;
    }

    JSONObject invokeJSON(String serviceName, String methodName, Object request) {
        String re = invoke(serviceName, methodName, request);
        return JSON.parseObject(re);
    }

    public JSONObject invokeJSON(Class<?> clazz, String methodName, Object object) {
        String serviceName = clazz.getName();
        return invokeJSON(serviceName, methodName, object);
    }

    /**
     * 不建议使用此种方式 重复服务名可能会混淆
     * @param str
     * @param request
     * @return
     */
    JSONObject invokeJSON(String str, Object request) {
        String serviceName = str.substring(0, str.indexOf(".")).trim();
        String methodName = str.substring(str.indexOf(".") + 1).trim();
        return invokeJSON(serviceName, methodName, request);
    }

    public <T> T invokeObject(Class<?> serviceClass, String methodName, Object request) {
        String serviceName = serviceClass.getName();
        String re = invoke(serviceName, methodName, request);
        return getObject(serviceClass, methodName, request, re);
    }
}
