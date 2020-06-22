package com.atomic.param.handler;

import com.atomic.param.Constants;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2017 09 30 17:55
 */
public class RandomParamHandler implements IHandler {

    private IHandler handler;

    private static int randomInt(String startInclusive, String endInclusive) {
        return RandomUtils.nextInt(Integer.parseInt(startInclusive), Integer.parseInt(endInclusive));
    }

    private static double randomDouble(String startInclusive, String endInclusive) {
        return RandomUtils.nextDouble(Double.parseDouble(startInclusive), Double.parseDouble(endInclusive));
    }

    private static long randomLong(String startInclusive, String endInclusive) {
        return RandomUtils.nextLong(Long.parseLong(startInclusive), Long.parseLong(endInclusive));
    }

    private static float randomFloat(String startInclusive, String endInclusive) {
        return RandomUtils.nextFloat(Float.parseFloat(startInclusive), Float.parseFloat(endInclusive));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void handle(T t) {
        if (Map.class.isInstance(t)) {
            Map<String, Object> param = (Map<String, Object>) t;
            param.keySet().stream().filter(key -> param.get(key) != null).forEach(key -> {
                Object value = param.get(key);
                String[] values = value.toString().split(":");
                if (values.length > 0) {
                    if (Constants.EXCEL_RANDOM.equals(values[0])) {
                        if ("String".equals(values[1])) {
                            param.put(key, RandomStringUtils.randomAlphabetic(Integer.parseInt(values[2])));
                        } else if ("int".equals(values[1])) {
                            param.put(key, randomInt(values[2], values[3]));
                        } else if ("double".equals(values[1])) {
                            param.put(key, randomDouble(values[2], values[3]));
                        } else if ("float".equals(values[1])) {
                            param.put(key, randomFloat(values[2], values[3]));
                        } else if ("long".equals(values[1])) {
                            param.put(key, randomLong(values[2], values[3]));
                        }
                    }
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
