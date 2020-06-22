package com.atomic;

import com.atomic.param.TestNGUtils;
import com.atomic.param.parser.ExcelResolver;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atomic.param.Constants.TEST_ONLY;
import static com.atomic.param.ParamUtils.isValueTrue;


/**
 * @author dreamyao
 * @title
 * @Data 2018/4/21 下午10:05
 * @since 1.0.0
 */
public abstract class AbstractRestTest {

    /**
     * 接口数据驱动
     * @param method
     * @return
     * @throws Exception
     */
    @DataProvider(name = "excel")
    public Iterator<Object[]> dataProvider(Method method) throws Exception {
        return readDataForTest(method);
    }

    /**
     * 接口数据驱动-并行
     * @param method
     * @return
     * @throws Exception
     */
    @DataProvider(name = "parallelExcel", parallel = true)
    public Iterator<Object[]> parallelDataProvider(Method method) throws Exception {
        return readDataForTest(method);
    }

    private Iterator<Object[]> readDataForTest(Method method) throws Exception {
        String className = this.getClass().getSimpleName();
        String resource = this.getClass().getResource("").getPath();
        String xls = resource + className + ".xls";
        ExcelResolver excelUtil = new ExcelResolver(xls, method.getName());
        List<Map<String, Object>> list = excelUtil.readDataByRow();
        Set<Object[]> set = new LinkedHashSet<>();
        // 如果有测试用例 testOnly 标志设置为1或者Y，则单独运行，其他没带此标志的测试用例跳过
        boolean hasTestOnly = list.stream().filter(map -> map.get(TEST_ONLY) != null)
                .anyMatch(newMap -> isValueTrue(newMap.get(TEST_ONLY)));
        for (Map<String, Object> map : list) {
            if (hasTestOnly) {
                if (!isValueTrue(map.get(TEST_ONLY))) {
                    continue;
                }
            }
            // 注入默认值，否则执行时参数不匹配
            set.add(TestNGUtils.injectResultAndParametersByDefault(map, method));
        }
        if (set.size() == 0) {
            Reporter.log("没有测试用例！");
            System.err.println("------------------没有测试用例！------------------");
        }
        return set.iterator();
    }
}
