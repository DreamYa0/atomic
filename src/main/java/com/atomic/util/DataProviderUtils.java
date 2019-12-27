package com.atomic.util;

import com.atomic.param.Constants;
import com.atomic.param.ParamUtils;
import com.atomic.param.TestNGUtils;
import com.atomic.param.parser.ExcelResolver;
import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dreamyao
 * @title
 * @date 2019-12-27 13:06
 * @since 1.0.0
 */
public abstract class DataProviderUtils {

    public static Iterator<Object[]> readExcel(Object obj, Method method) throws Exception {
        String className = obj.getClass().getSimpleName();
        String resource = obj.getClass().getResource("").getPath();
        String xls = resource + className + ".xls";
        ExcelResolver excelUtil = new ExcelResolver(xls, method.getName());
        List<Map<String, Object>> list = excelUtil.readDataByRow();
        Set<Object[]> set = Sets.newLinkedHashSet();
        // 如果有测试用例 testOnly 标志设置为1或者Y，则单独运行，其他没带此标志的测试用例跳过
        boolean hasTestOnly = false;
        for (Map<String, Object> map : list) {
            if (ParamUtils.isValueTrue(map.get(Constants.TEST_ONLY))) {
                hasTestOnly = true;
                break;
            }
        }
        for (Map<String, Object> map : list) {
            if (hasTestOnly) {
                if (!ParamUtils.isValueTrue(map.get(Constants.TEST_ONLY))) {
                    continue;
                }
            }
            // 注入默认值，否则执行时参数不匹配
            set.add(TestNGUtils.injectResultAndParametersByDefault(map, method));
        }
        return set.iterator();
    }
}
