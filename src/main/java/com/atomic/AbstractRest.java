package com.atomic;

import com.google.common.collect.Sets;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import static com.atomic.param.util.DataProviderUtils.iterCaseFromExcel;
import static com.atomic.param.util.DataProviderUtils.iterCaseFromYaml;


/**
 * @author dreamyao
 * @title
 * @date  2018/4/21 下午10:05
 * @since 1.0.0
 */
public abstract class AbstractRest {

    // @DataProvider(name = "excel")
    @DataProvider(name = "cases")
    public Iterator<Object[]> dataProvider(Method method) throws Exception {
        // 接口数据驱动
         return readDataForTest(method);
    }

    // @DataProvider(name = "parallelExcel", parallel = true)
    @DataProvider(name = "parallelCases", parallel = true)
    public Iterator<Object[]> parallelDataProvider(Method method) throws Exception {
        // 接口数据驱动-并行
         return readDataForTest(method);
    }

//    private Iterator<Object[]> readDataForTest(Method method) throws Exception {
//        String className = this.getClass().getSimpleName();
//        String resource = this.getClass().getResource("").getPath();
//        String xls = resource + className + ".xls";
//        ExcelResolver excelUtil = new ExcelResolver(xls, method.getName());
//        List<Map<String, Object>> list = excelUtil.readDataByRow();
//        Set<Object[]> set = new LinkedHashSet<>();
//        // 如果有测试用例 testOnly 标志设置为1或者Y，则单独运行，其他没带此标志的测试用例跳过
//        boolean hasTestOnly = list.stream().filter(map -> map.get(TEST_ONLY) != null)
//                .anyMatch(newMap -> isValueTrue(newMap.get(TEST_ONLY)));
//        for (Map<String, Object> map : list) {
//            if (hasTestOnly) {
//                if (!isValueTrue(map.get(TEST_ONLY))) {
//                    continue;
//                }
//            }
//            // 注入默认值，否则执行时参数不匹配
//            set.add(TestNGUtils.injectResultAndParametersByDefault(map, method));
//        }
//        if (set.size() == 0) {
//            Reporter.log("没有测试用例！");
//            System.err.println("------------------没有测试用例！------------------");
//        }
//        return set.iterator();
//    }

    private Iterator<Object[]> readDataForTest(Method method) throws Exception {
        File caseFile = readCaseFile();

        if (caseFile.getAbsolutePath().substring(caseFile.getAbsolutePath().length() - 5).equals(".yaml")) {
            return iterCaseFromYaml(caseFile, method);
        } else if (caseFile.getAbsolutePath().substring(-4).equals(".xml")) {
            Set<Object[]> set = Sets.newLinkedHashSet();

            return set.iterator();
        } else if (caseFile.getAbsolutePath().substring(-4).equals(".xls")) {
            return iterCaseFromExcel(caseFile.getAbsolutePath(), method);
        } else {
            Set<Object[]> set = Sets.newLinkedHashSet();

            return set.iterator();
        }
    }

    /**
     * 按优先级返回文件对象
     * priority: yaml->xml->xls
     * @return
     */
    private File readCaseFile() {
        String className = this.getClass().getSimpleName();
        String resource = this.getClass().getResource("").getPath();

        // 优先级:1，读取yaml文件
        File fileYaml = new File(resource + className + ".yaml");
        if (fileYaml.exists()) {
            return fileYaml;
        }

        // 优先级:2，读取xml文件
        File fileXml = new File(resource + className + ".xml");
        if (fileXml.exists()) {
            return fileXml;
        }

        // 优先级:3，读取excel文件
        File fileExcel = new File(resource + className + ".xls");
        if (fileExcel.exists()) {
            return fileExcel;
        }

        return null;
    }
}
