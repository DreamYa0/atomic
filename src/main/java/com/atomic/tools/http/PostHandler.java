package com.atomic.tools.http;

import com.alibaba.fastjson.JSON;
import com.atomic.param.Constants;
import com.atomic.param.ParamUtils;

import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0.0
 * @description
 * @Data 2017/10/25 10:10
 */
public class PostHandler implements IHandler {

    private IHandler handler;

    @Override
    @SuppressWarnings("unchecked")
    public <T> HttpResponse handle(T t, HttpRequest request) {
        if (Map.class.isInstance(t)) {
            Map<String, Object> param = (Map<String, Object>) t;
            boolean isPost = Constants.HTTP_POST.equalsIgnoreCase(param.get(Constants.HTTP_MODE).toString());
            if (isPost) {
                request.method(Method.POST);
                if (param.get(Constants.CONTENT_TYPE) == null ||
                        Constants.CONTENT_TYPE_FROM.equalsIgnoreCase(param.get(Constants.CONTENT_TYPE).toString())) {
                    request.contentType(Constants.CONTENT_TYPE_FROM);
                    Map<String, Object> newParam = ParamUtils.getParameters(param);
                    request.form(newParam);
                    return request.execute();
                } else if (Constants.CONTENT_TYPE_JSON.equalsIgnoreCase(param.get(Constants.CONTENT_TYPE).toString())) {
                    Map<String, Object> newParam = ParamUtils.getParameters(param);
                    request.contentType(Constants.CONTENT_TYPE_JSON).body(JSON.toJSONString(newParam));
                    return request.execute();
                }
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
