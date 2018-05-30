package com.atomic.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atomic.exception.JSONCheckException;
import org.testng.Reporter;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * JSON断言工具类
 * @author Yangminhan
 * @version 1.0
 *          <p/>
 *          Created by Yangminhan on 2017/6/13.
 */
public final class AssertJSONUtils {

    private static JSONArray jsonArray;

    static {
        jsonArray = new JSONArray();
    }

    private AssertJSONUtils() {
    }

    /**
     * 查找JSONObject中的key，如果key对应value为JSONObject或JSONArray，则返回JSONArray对象
     * @param actJSON
     * @param key
     * @return
     * @throws JSONCheckException
     */
    public static JSONArray getJsonArray(JSONObject actJSON, String key) throws JSONCheckException {
        clearJSONArray();
        setJsonArray(actJSON, key);

        if (null == jsonArray || jsonArray.size() == 0) {
            Reporter.log("actJSON对象未包含key:" + key);
            throw new JSONCheckException("actJSON对象未包含key:" + key);
        }

        return jsonArray;
    }

    /**
     * 遍历actJSON查找key值对应的JSONObject或JSONArray对象
     * @param actJSON
     * @param key
     * @throws JSONCheckException
     */
    private static void setJsonArray(JSONObject actJSON, String key) throws JSONCheckException {
        if (!hasSubJSON(actJSON)) {
            //JSONObject对象中包含该key则获取，未包含则递归
            if (actJSON.containsKey(key)) {
                Set<Map.Entry<String, Object>> jsonSet = actJSON.entrySet();

                Iterator<Map.Entry<String, Object>> iterator = jsonSet.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> map = iterator.next();

                    if (map.getKey().equals(key)) {
                        if (map.getValue().getClass().isInstance(new JSONObject())) {
                            jsonArray.add(actJSON.getJSONObject(key));
                        }

                        if (map.getValue().getClass().isInstance(new JSONArray())) {
                            jsonArray = actJSON.getJSONArray(key);
                        }
                    }
                }
            } else {
                Set<Map.Entry<String, Object>> jsonSet = actJSON.entrySet();

                Iterator<Map.Entry<String, Object>> iterator = jsonSet.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> map = iterator.next();

                    if (map.getValue().getClass().isInstance(new JSONObject())) {
                        JSONObject subJson = (JSONObject) map.getValue();
                        setJsonArray(subJson, key);
                    }

                    if (map.getValue().getClass().isInstance(new JSONArray())) {
                        JSONArray subJsonArray = (JSONArray) map.getValue();
                        if (subJsonArray.size() > 0) {
                            for (int i = 0; i < subJsonArray.size(); i++) {
                                if (subJsonArray.get(i) instanceof JSONObject) {
                                    JSONObject tempJson = (JSONObject) subJsonArray.get(i);

                                    setJsonArray(tempJson, key);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * actJSON包含expJSON全部字段，则返回true
     * @param actJSON
     * @param expJSON
     * @return
     * @throws JSONCheckException
     */
    public static boolean containsJSON(JSONObject actJSON, JSONObject expJSON) throws JSONCheckException {
        boolean status = true;

        // 如果actJSON未嵌套JSONObject或JSONArray，则比较actJSON和expJSON
        if (hasSubJSON(actJSON)) {
            Set<Map.Entry<String, Object>> jsonSet = expJSON.entrySet();

            Iterator<Map.Entry<String, Object>> iterator = jsonSet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> map = iterator.next();

                String key = map.getKey().toString();
                String value = map.getValue().toString();
                // 如果actJSON未包含expect key，则返回false
                if (actJSON.containsKey(key)) {
                    // 如果actJSON和expJSON中相同key对应的value不一致，则返回false
                    if (!actJSON.get(key).toString().equals(value)) {
                        status = false;
                    }
                } else {
                    status = false;
                }
            }
        } else {
            Reporter.log("被断言actJSON对象中嵌套有JSONObject或JSONArray.");
            throw new JSONCheckException("被断言actJSON对象中嵌套有JSONObject或JSONArray.");
        }

        return status;
    }

    /**
     * JSONArray中任意JSONObject对象包含expJSON全部字段，则返回true
     * @param jsonArray
     * @param expJSON
     * @return
     * @throws JSONCheckException
     */
    public static boolean containsJSON(JSONArray jsonArray, JSONObject expJSON) throws JSONCheckException {
        boolean status = false;

        if (null != jsonArray && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.get(i).getClass().isInstance(new JSONObject())) {
                    JSONObject json = (JSONObject) jsonArray.get(i);

                    if (hasSubJSON(json)) {
                        if (containsJSON(json, expJSON)) {
                            status = true;
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        Reporter.log("被断言JSONArray嵌套的JSONObject包含有JSONObject或JSONArray.");
                        throw new JSONCheckException("被断言JSONArray嵌套的JSONObject包含有JSONObject或JSONArray.");
                    }
                } else {
                    Reporter.log("被断言JSONArray嵌套的对象非JSONObject.");
                    throw new JSONCheckException("被断言JSONArray嵌套的对象非JSONObject.");
                }
            }
        } else {
            Reporter.log("被断言JSONArray对象 not be null or empty.");
            throw new JSONCheckException("被断言JSONArray对象 not be null or empty.");
        }

        return status;
    }


    /**
     * 判断JSONObject对象是否嵌套JSONObject和JSONArray
     * @param json
     * @return 未嵌套JSONObject或JSONArray，返回true，否则返回false
     * @throws JSONCheckException
     */
    private static boolean hasSubJSON(JSONObject json) throws JSONCheckException {
        boolean status = true;

        if (null != json && !json.isEmpty()) {
            Set<Map.Entry<String, Object>> jsonSet = json.entrySet();

            Iterator<Map.Entry<String, Object>> iterator = jsonSet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> map = iterator.next();

                if (map.getValue().getClass().isInstance(new JSONObject())) {
                    status = false;
                    break;
                }

                if (map.getValue().getClass().isInstance(new JSONArray())) {
                    status = false;
                    break;
                }
            }
        } else {
            throw new JSONCheckException("JSONObject not be null or empty.");
        }

        return status;
    }

    /**
     * 初始化JSONArray的元素
     */
    private static void clearJSONArray() {
        if (null != jsonArray && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonArray.remove(i);
            }
        }
    }

    /*public static void main(String args[]) {
        JSONObject testJSON = JSONObject.parseObject("{\"classNumber\":1,\"classProperty\":{\"name\":\"value\"},\"students\":[{\"name\":\"lilei\",\"age\":10,\"grade\":{\"mathematics\":80,\"language\":90}},{\"name\":\"hanmeimei\",\"age\":10,\"grade\":{\"mathematics\":80,\"language\":88}},{\"name\":\"zhangsan\",\"age\":11,\"grade\":{\"mathematics\":80,\"language\":90}},{\"name\":\"lisi\",\"age\":9,\"grade\":{\"mathematics\":70,\"language\":90}}]}");

        try {
            JSONArray actJSON = getJsonArray(testJSON, "grade");
            JSONObject expJSON = JSONObject.parseObject("{\"mathematics\":80}");
            System.out.println(containsJSON(actJSON, expJSON));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
