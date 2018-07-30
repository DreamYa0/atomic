package com.atomic;

import com.atomic.listener.SoaMockListener;
import com.atomic.param.Constants;
import com.atomic.param.ParamUtils;
import com.atomic.param.TestNGUtils;
import com.atomic.param.parser.ExcelResolver;
import com.google.common.collect.Sets;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
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
        ExcelResolver excelUtil = new ExcelResolver(xls,method.getName());
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

