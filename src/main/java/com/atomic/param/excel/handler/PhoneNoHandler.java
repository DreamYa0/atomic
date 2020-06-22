package com.atomic.param.excel.handler;

import com.atomic.param.Constants;

import java.util.Map;
import java.util.Objects;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2017 10 01 14:16
 */
public class PhoneNoHandler implements IHandler {

    private IHandler handler;
    private String[] telFirst = ("134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156," +
            "133,153").split(",");

    @Override
    @SuppressWarnings("unchecked")
    public <T> void handle(T t) {
        if (Map.class.isInstance(t)) {
            Map<String, Object> param = (Map<String, Object>) t;
            param.forEach((key, value) -> {
                if (Objects.nonNull(value) && Constants.PHONE_NO.equals(value.toString())) {
                    param.put(key, getTel());
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

    private String getTel() {
        int index = getNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        String third = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        return first + second + third;
    }

    private int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }
}
