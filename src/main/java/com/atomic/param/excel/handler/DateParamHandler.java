package com.atomic.param.excel.handler;

import com.atomic.param.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date  2018/05/30 10:48
 */
public class DateParamHandler implements IHandler {

    private IHandler handler;

    @Override
    @SuppressWarnings("unchecked")
    public <T> void handle(T t) {
        if (Map.class.isInstance(t)) {
            Map<String, Object> param = (Map<String, Object>) t;
            param.forEach((key, value) -> {
                if (Objects.nonNull(value) && Constants.NOW_DAY.equals(value.toString())) {
                    param.put(key, dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
                } else if (Objects.nonNull(value) && value.toString().startsWith("${now()-")) {
                    String numStr = value.toString().substring(8, 9);
                    Integer num = Integer.valueOf(numStr);
                    if (value.toString().endsWith("D}")) {
                        Date beforeDay = getBeforeDay(num);
                        param.put(key, dateToStr(beforeDay, "yyyy-MM-dd HH:mm:ss"));
                    } else if (value.toString().endsWith("M}")) {
                        Date beforeMonth = getBeforeMonth(num);
                        param.put(key, dateToStr(beforeMonth, "yyyy-MM-dd HH:mm:ss"));
                    }
                }
            });
        }
        if (handler != null) {
            handler.handle(t);
        }
    }

    /**
     * 获取当前时间前多少天的时间
     * @param num 天数
     * @return 返回多少天之前的时间
     */
    private Date getBeforeDay(int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -num);
        return calendar.getTime();
    }

    /**
     * 获取多少月之前的时间
     * @param month 月数，1：表示一个月之前，2：表示2个月之前
     * @return 返回几月之前的时间
     */
    private Date getBeforeMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -month);
        return calendar.getTime();
    }

    private String dateToStr(Date date, String pattern) {
        return dateToStr(date, pattern, Locale.CHINA);
    }

    private String dateToStr(Date date, String pattern, Locale locale) {
        if (pattern == null) {
            pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        }
        DateFormat ymdhmsFormat = new SimpleDateFormat(pattern, locale);

        return ymdhmsFormat.format(date);
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
