package com.atomic.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.util.Date;

/**
 * @author dreamyao
 * @title
 * @date 2020/6/22 5:38 PM
 * @since 1.0.0
 */
public class GsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) ->
                    new Date(json.getAsJsonPrimitive().getAsLong()))
            // 设置时间格式
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            // IDENTITY域命名策略将会使用和Java模型完全一样的名称
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            // 强制序列化null值
            .serializeNulls()
            // 使用仁慈的Gson（容错机制）
            .setLenient()
            .create();

    public static Gson getGson() {
        return GSON;
    }
}
