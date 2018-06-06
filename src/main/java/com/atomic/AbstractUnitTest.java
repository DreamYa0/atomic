package com.atomic;

import com.atomic.listener.SoaMockListener;
import com.atomic.param.Constants;
import com.atomic.param.ParamUtils;
import com.atomic.param.TestNGUtils;
import com.atomic.util.CsvUtils;
import com.atomic.util.ExcelUtils;
import com.google.common.collect.Sets;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


// @ContextConfiguration(locations = {"/test-service.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, SoaMockListener.class})
public abstract class AbstractUnitTest extends AbstractTestNGSpringContextTests {

    @DataProvider(name = "excel")
    public Iterator<Object[]> dataProvider(Method method) throws Exception {
        return readDataForIntegration(method);
    }

    @DataProvider(name = "parallelExcel", parallel = true)
    public Iterator<Object[]> parallelDataProvider(Method method) throws Exception {
        return readDataForIntegration(method);
    }

    private Iterator<Object[]> readDataForIntegration(Method method) throws Exception {
        String className = this.getClass().getSimpleName();
        String resource = this.getClass().getResource("").getPath();
        String xls = resource + className + ".xls";
        ExcelUtils excelUtil = new ExcelUtils();
        List<Map<String, Object>> list = excelUtil.readDataByRow(xls, method.getName());
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

    @DataProvider(name = "csv")
    public Iterator<Object[]> dataProviderFromCsv(Method method) {
        Set<Object[]> set = new HashSet<Object[]>();
        try {
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
            Iterator<Object[]> iterator = set.iterator();
            return iterator;
        } catch (Exception e) {
            Reporter.log("[AbstractUnitTest#dataProviderFromCsv()]:{dataProvider处理失败！}");
            System.out.println("--------------dataProvider处理失败！----------------");
            e.printStackTrace();
            return set.iterator();
        }
    }

    private void handleParams(List<Map<String, Object>> list, Set<Object[]> set, Method method) throws Exception {
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
        if (set.size() == 0) {
            Reporter.log("[CommonTest#handleParams()]:{没有测试用例！}");
            System.err.println("------------------没有测试用例！------------------");
        }
    }
}

