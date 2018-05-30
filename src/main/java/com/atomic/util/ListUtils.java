package com.atomic.util;

import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ListUtils {

    private ListUtils() {
    }

    /**
     * 去除List中重复的对象
     * @param list       list集合
     * @param comparator 比较器
     * @param <T>        类型
     * @return 去重后的List集合
     */
    public static <T> List<T> distinct(List<T> list, Comparator<T> comparator) {
        if (CollectionUtils.isEmpty(list) || comparator == null)
            return list;
        List<T> newList = new ArrayList<>(list.size());
        list.stream().filter(t -> !contains(newList, t, comparator)).forEach(newList::add);
        return newList;
    }

    /**
     * 去除List中重复的Map
     * @param mapList list集合
     * @return 去重后的List集合
     */
    public static List<Map<String, Object>> distinctMap(List<Map<String, Object>> mapList) {
        return distinct(mapList, (o1, o2) -> {
            for (int i = 0; i < o1.keySet().size(); i++) {
                Iterator<String> keys = o1.keySet().iterator();
                while (keys.hasNext()) {
                    if (o1.get(keys.next()).toString().compareTo(o2.get(keys.next()).toString()) == 0) {
                        return 0;
                    }
                }
            }
            return 1;
        });
    }

    /**
     * 判断List中是否包含此对象
     * @param list       list集合
     * @param target     目标对象
     * @param comparator 比较器
     * @param <T>        对象
     * @return true 或 false
     */
    private static <T> boolean contains(List<T> list, T target, Comparator<T> comparator) {
        for (T t : list) {
            if (comparator.compare(t, target) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据多属性进行list转map分组
     * @param list    要转换的集合
     * @param strings 作为key的string数组
     * @param <T>     集合里对象的泛型
     * @return list集合
     */
    public static <T> Map<String, List<T>> list2Map(List<? extends T> list, String... strings) {
        Map<String, List<T>> returnMap = new HashMap<>();
        try {
            for (T t : list) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : strings) {
                    // 通过反射获得私有属性,这里捕获获取不到属性异常
                    Field name1 = t.getClass().getDeclaredField(s);
                    // 获得访问和修改私有属性的权限
                    name1.setAccessible(true);
                    // 获得key值
                    String key = name1.get(t).toString();
                    stringBuilder.append(key);
                }
                String KeyName = stringBuilder.toString();

                List<T> tempList = returnMap.get(KeyName);
                if (tempList == null) {
                    tempList = Lists.newArrayList();
                    tempList.add(t);
                    returnMap.put(KeyName, tempList);
                } else {
                    tempList.add(t);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return returnMap;
    }
}
