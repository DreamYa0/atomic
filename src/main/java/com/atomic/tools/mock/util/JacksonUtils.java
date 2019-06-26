package com.atomic.tools.mock.util;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JacksonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    public static String encode(Object object) {

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object decode(String value, Class clazz) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        try {
            return objectMapper.readValue(value, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List decodeArray(String value, String clazz) {
        List resultArray = (List) decode(value, List.class);
        if (CollectionUtils.isEmpty(resultArray)) {
            return Collections.emptyList();
        }


        try {
            ArrayList<Object> rets = Lists.newArrayList();
            for (Object object : resultArray) {
                rets.add(decode(objectMapper.writeValueAsString(object), Class.forName(clazz)));
            }
            return rets;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
