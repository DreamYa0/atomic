package com.atomic.param.util;

import com.atomic.param.Constants;
import com.atomic.param.excel.parser.ExcelResolver;
import com.atomic.param.excel.parser.YamlResolver;
import com.atomic.util.TestNGUtils;
import com.google.common.collect.Sets;
import org.testng.Reporter;

import java.io.File;
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

    /**
     * 读取测试用例数据并注入testNG上下文
     * priority: yaml->xml->xls
     * xml文件读取暂不支持 2021-01-18
     */
    public static Iterator<Object[]> readDataSource(Object obj, Method method) throws Exception {
        String className = obj.getClass().getSimpleName();
        String resource = obj.getClass().getResource("").getPath();
        File caseFile = readCaseFile(resource + className);

        if (caseFile.getAbsolutePath().endsWith(".yaml")) {
            return iterCaseFromYaml(caseFile, method);
        } else if (caseFile.getAbsolutePath().endsWith(".xls")) {
            return iterCaseFromExcel(caseFile.getAbsolutePath(), method);
        } else {
            Set<Object[]> set = Sets.newLinkedHashSet();
            return set.iterator();
        }
    }

    /**
     * 按优先级返回文件对象
     * priority: yaml->xml->xls
     *
     * @param filePath
     * @return
     */
    private static File readCaseFile(String filePath) {
        // 优先级:1，读取yaml文件
        File fileYaml = new File(filePath + ".yaml");
        if (fileYaml.exists()) {
            return fileYaml;
        }

        // 优先级:2，读取xml文件
        File fileXml = new File(filePath + ".xml");
        if (fileXml.exists()) {
            return fileXml;
        }

        // 优先级:3，读取excel文件
        File fileExcel = new File(filePath + ".xls");
        if (fileExcel.exists()) {
            return fileExcel;
        }

        throw new RuntimeException("文件不存在, 请检查文件!");
    }

    /**
     * 提取yaml文件测试用例并注入testNG上下文
     *
     * @param yamlFile
     * @param method
     * @return
     * @throws Exception
     */
    public static Iterator<Object[]> iterCaseFromYaml(File yamlFile, Method method) throws Exception {
        // 获取接口已定义测试用例
        YamlResolver yamlResolver = new YamlResolver();
        List<Map<String, Object>> list = yamlResolver.getCaseListFromYaml(yamlFile);

        Set<Object[]> set = Sets.newLinkedHashSet();
        // 如果有测试用例 testOnly 标志设置为true或者True, TRUE，则单独运行，其他没带此标志的测试用例跳过
        for (Map<String, Object> map : list) {
            if (!ParamUtils.isValueTrue(map.get(Constants.TEST_ONLY))) {
                continue;
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

    /**
     * 提取excel文件测试用例并注入testNG上下文
     *
     * @param excelFile
     * @param method
     * @return
     * @throws Exception
     */
    public static Iterator<Object[]> iterCaseFromExcel(String excelFile, Method method) throws Exception {
        // 获取接口已定义测试用例
        ExcelResolver excelUtil = new ExcelResolver(excelFile, method.getName());
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
        if (set.size() == 0) {
            Reporter.log("没有测试用例！");
            System.err.println("------------------没有测试用例！------------------");
        }
        return set.iterator();
    }
}
