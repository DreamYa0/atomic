package com.atomic.util;

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

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Date.class,
            (JsonDeserializer<Date>) (json, typeOfT, context) ->
                    new Date(json.getAsJsonPrimitive().getAsLong())).setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static Gson getGson() {
        return GSON;
    }
}
