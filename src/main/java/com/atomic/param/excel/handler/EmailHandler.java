package com.atomic.param.excel.handler;

import com.atomic.param.Constants;

import java.util.Map;
import java.util.Objects;


/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2017 10 01 14:17
 */
public class EmailHandler implements IHandler {

    private final String[] email_suffix = ("@gmail.com,@yahoo.com,@msn.com,@hotmail.com,@aol.com," +
            "@ask.com,@live.com,@qq.com,@0355.net,@163.com,@163.net,@263.net,@3721.net,@yeah.net," +
            "@googlemail.com,@126.com,@sina.com,@sohu.com,@yahoo.com.cn").split(",");
    public String base = "abcdefghijklmnopqrstuvwxyz0123456789";
    private IHandler handler;

    @Override
    @SuppressWarnings("unchecked")
    public <T> void handle(T t) {
        if (Map.class.isInstance(t)) {
            Map<String, Object> param = (Map<String, Object>) t;
            param.forEach((key, value) -> {
                if (Objects.nonNull(value) && Constants.EMAIL.equals(value.toString())) {
                    param.put(key, getEmail());
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

    private String getEmail() {
        int length = getNum(5, 12);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = (int) (Math.random() * base.length());
            sb.append(base.charAt(number));
        }
        sb.append(email_suffix[(int) (Math.random() * email_suffix.length)]);
        return sb.toString();
    }

    private int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }
}
