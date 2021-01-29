package com.atomic.tools.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.atomic.param.Constants;
import com.atomic.param.util.ParamUtils;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2018/05/30 10:48
 */
public class GetHandler implements IHandler {

    private IHandler handler;

    @Override
    @SuppressWarnings("unchecked")
    public <T> HttpResponse handle(T t, HttpRequest request) {
        if (Map.class.isInstance(t)) {
            Map<String, Object> context = (Map<String, Object>) t;

            Map<String, Object> newContext = Maps.newHashMap(context);

            boolean isGet = Constants.HTTP_GET.equalsIgnoreCase(newContext.get(Constants.HTTP_MODE).toString());
            if (isGet) {
                request.method(Method.GET);
                Map<String, Object> newParam = ParamUtils.getParameters(newContext);
                // TODO: 2018/6/9 递归处理map
                if (!newParam.isEmpty()) {
                    if (newParam.containsKey("expectResult")) {
                        newParam.remove("expectResult");
                    }
                    request.form(newParam);
                }
                return request.execute();
            }
        }
        if (handler != null) {
            handler.handle(t, request);
        }
        return null;
    }

    @Override
    public void setHandler(IHandler handler) {
        this.handler = handler;
    }
}
