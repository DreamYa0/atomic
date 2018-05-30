package com.atomic.tools.http;

import com.atomic.param.Constants;
import com.atomic.param.ParamUtils;

import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0.0
 * @description
 * @Data 2018/05/30 10:48
 */
public class GetHandler implements IHandler {

    private IHandler handler;

    @Override
    @SuppressWarnings("unchecked")
    public <T> HttpResponse handle(T t, HttpRequest request) {
        if (Map.class.isInstance(t)) {
            Map<String, Object> param = (Map<String, Object>) t;
            boolean isGet = Constants.HTTP_GET.equalsIgnoreCase(param.get(Constants.HTTP_MODE).toString());
            if (isGet) {
                request.method(Method.GET);
                Map<String, Object> newParam = ParamUtils.getParameters(param);
                if (!newParam.isEmpty()) {
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
