package com.atomic.param.excel.handler;

import com.atomic.param.Constants;

import java.util.Map;
import java.util.Objects;


/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2017 09 30 18:11
 */
public class IdCardHandler implements IHandler {

    private IHandler handler;

    @Override
    @SuppressWarnings("unchecked")
    public <T> void handle(T t) {
        if (Map.class.isInstance(t)) {
            Map<String, Object> param = (Map<String, Object>) t;
            param.forEach((key, value) -> {
                if (Objects.nonNull(value)&&Constants.ID_CARD.equals(value.toString())) {
                    value = IdCardParam.newInstance().generate();
                    param.put(key, value);
                }
            });
        }
        if (handler != null) {
            handler.handle(t);
        }
    }

    @Override
    public <T> T handle2Result(T t) {
        return null;
    }

    @Override
    public void setHandler(IHandler handler) {
        this.handler = handler;
    }
}
