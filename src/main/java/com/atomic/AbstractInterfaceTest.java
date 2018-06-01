package com.atomic;

import com.atomic.param.TestNGUtils;
import com.atomic.util.CsvUtils;
import com.atomic.util.ExcelUtils;
import com.google.common.collect.Sets;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.FileNotFoundException;
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
public abstract class AbstractInterfaceTest {
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
        ExcelUtils excelUtil = new ExcelUtils();
        List<Map<String, Object>> list = excelUtil.readDataByRow(xls, method.getName());
        Set<Object[]> set = new LinkedHashSet<>();
        handleParams(list, set, method);
        return set.iterator();
    }

    /**
     * 入参数据处理
     * @param list
     * @param set
     * @param method
     * @throws Exception
     */
    private void handleParams(List<Map<String, Object>> list, Set<Object[]> set, Method method) throws Exception {
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
            Reporter.log("[CommonTest#handleParams()]:{没有测试用例！}");
            System.err.println("------------------没有测试用例！------------------");
        }
    }

    @DataProvider(name = "csv")
    public Iterator<Object[]> dataProviderFromCsv(Method method) {
        Set<Object[]> set = Sets.newLinkedHashSet();
        try {
            // 获取测试用例Excel文件路径
            String className = this.getClass().getSimpleName();
            String resource = this.getClass().getResource("").getPath();
            String path = resource + className + ".csv";
            File file = new File(path);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
            List<Map<String, Object>> list = CsvUtils.handleFile(file);
            // 处理入参
            handleParams(list, set, method);
            return set.iterator();
        } catch (Exception e) {
            Reporter.log("[CommonTest#dataProviderFromCsv()]:{dataProvider处理失败！}");
            System.out.println("--------------dataProvider处理失败！----------------");
            e.printStackTrace();
            return set.iterator();
        }
    }
}
