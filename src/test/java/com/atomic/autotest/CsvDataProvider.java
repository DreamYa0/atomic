package com.atomic.autotest;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dreamyao
 * @title
 * @date 2021/9/5 11:21 下午
 * @since 1.0.0
 */
public class CsvDataProvider {

    /**
     * 如果有测试用例此标志设置为Y，则单独运行，其他没带此标志的测试用例跳过
     */
    private static final String TEST_ONLY = "testOnly";
    /**
     * excel中Y字段值
     */
    private static final String EXCEL_YES = "Y";
    private final CsvReader reader = CsvUtil.getReader();

    @DataProvider(name = "csv")
    public Iterator<Object[]> dataProvider(Method method) throws Exception {
        String className = this.getClass().getSimpleName();
        String resource = this.getClass().getResource("").getPath();
        String csv = resource + className + ".csv";

        final List<Map<String, String>> list = reader.readMapList(ResourceUtil.getUtf8Reader(csv));

        Set<Object[]> set = new LinkedHashSet<>();
        // 如果有测试用例 testOnly 标志设置为1或者Y，则单独运行，其他没带此标志的测试用例跳过
        boolean hasTestOnly = list.stream().filter(map -> map.get(TEST_ONLY) != null)
                .anyMatch(newMap -> isValueTrue(newMap.get(TEST_ONLY)));

        // TODO 这里获取token

        for (Map<String, String> map : list) {
            if (hasTestOnly) {
                if (!isValueTrue(map.get(TEST_ONLY))) {
                    continue;
                }
            }
            //TODO 这里把token设置到测试数据里面去
            map.put("token", "");
            // 注入默认值，否则执行时参数不匹配
            set.add(injectResultAndParametersByDefault(map, method));
        }
        if (set.size() == 0) {
            Reporter.log("没有测试用例！");
            System.err.println("------------------没有测试用例！------------------");
        }
        return set.iterator();
    }

    private Object[] injectResultAndParametersByDefault(Map<String, String> map,
                                                        Method testMethod) throws Exception {
        // 把参数和结果注入到测试函数，先注入默认值，防止执行时参数不匹配
        int parameters = testMethod.getGenericParameterTypes().length;
        Object[] objects;
        if (parameters > 1) {
            objects = new Object[parameters];
            objects[0] = map;
            // 第一个参数是Map<String,Object> context，不处理；第二个参数是Object result
            for (int i = 1; i < parameters; i++) {
                objects[i] = null;
            }
        } else {
            objects = new Object[]{map};
        }
        return objects;
    }

    private boolean isValueTrue(Object value) {
        // 只有 1 或 Y 返回true
        return value != null && ("1".equalsIgnoreCase(value.toString()) ||
                EXCEL_YES.equalsIgnoreCase(value.toString()));
    }
}
