package com.atomic.listener;

import com.atomic.config.GlobalConfig;
import com.atomic.param.Constants;
import com.atomic.param.HandleMethodName;
import com.atomic.param.TestNGUtils;
import com.atomic.param.entity.AutoTestResult;
import com.atomic.util.CIDbUtils;
import com.atomic.param.parser.ExcelResolver;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.TestAttribute;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.restassured.response.Response;
import org.springframework.util.CollectionUtils;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.xml.XmlSuite;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Comparator.comparing;

/**
 * 通用测试结果处理监听器
 * @author dreamyao
 * @version 2.0.0           Created by dreamyao on 2017/6/7.
 */
public class ReportListener implements IReporter {

    private ExtentReports extent;
    private String projectName;
    private String[] runAuthor;
    private List<Integer> failedTests = Lists.newArrayList();
    private List<Integer> skippedTests = Lists.newArrayList();
    private List<Integer> passedTests = Lists.newArrayList();
    private List<Integer> allTestMethod = Lists.newArrayList();

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        boolean createSuiteNode = false;
        if (suites.size() > 1) {
            createSuiteNode = true;
        }
        if (suites.size() > 0) {
            for (ISuite suite : suites) {
                Map<String, ISuiteResult> iSuiteResultMap = suite.getResults();
                // 如果suite里面没有任何用例，直接跳过，不在报告里生成
                if (iSuiteResultMap.size() == 0) {
                    continue;
                }
                // 统计suite下的成功、失败、跳过的总用例数
                int suiteFailSize = 0;
                int suitePassSize = 0;
                int suiteSkipSize = 0;
                ExtentTest suiteTest = null;

                // 加载环境配置文件
                GlobalConfig.load();
                this.projectName = GlobalConfig.projectName;
                String runAuthors = GlobalConfig.runner;
                this.runAuthor = runAuthors.split(",");

                extent = ExtentManager.getInstance(projectName);
                // 存在多个suite的情况下，在报告中将同一个一个suite的测试结果归为一类，创建一级节点。
                if (createSuiteNode) {
                    suiteTest = extent.createTest(suite.getName()).assignCategory(projectName);
                    suiteTest.assignAuthor(runAuthor);
                }
                boolean createSuiteResultNode = false;
                if (iSuiteResultMap.size() > 1) {
                    createSuiteResultNode = true;
                }
                for (ISuiteResult r : iSuiteResultMap.values()) {
                    ExtentTest resultNode;
                    ITestContext context = r.getTestContext();

                    if (createSuiteResultNode) {
                        // 没有创建suite的情况下，将在SuiteResult的创建为一级节点，否则创建为suite的一个子节点。
                        if (null == suiteTest) {
                            resultNode = extent.createTest(r.getTestContext().getName()).assignCategory(projectName);
                            resultNode.assignAuthor(runAuthor);
                            resultNode.getModel().setStartTime(context.getStartDate());
                        } else {
                            resultNode = suiteTest.createNode(r.getTestContext().getName());
                        }
                    } else {
                        resultNode = suiteTest;
                    }
                    if (resultNode != null) {
                        resultNode.getModel().setName(suite.getName() + " : " + r.getTestContext().getName());
                        /*if (resultNode.getModel().hasCategory()) {
                            resultNode.assignCategory(r.getTestContext().getName());
                        } else {
                            resultNode.assignCategory(suite.getName(), r.getTestContext().getName());
                        }*/
                        resultNode.getModel().setStartTime(r.getTestContext().getStartDate());
                        resultNode.getModel().setEndTime(r.getTestContext().getEndDate());
                        //统计SuiteResult下的数据
                        int passSize = r.getTestContext().getPassedTests().size();
                        int failSize = r.getTestContext().getFailedTests().size();
                        int skipSize = r.getTestContext().getSkippedTests().size();
                        suitePassSize += passSize;
                        suiteFailSize += failSize;
                        suiteSkipSize += skipSize;
                        if (failSize > 0) {
                            resultNode.getModel().setStatus(Status.FAIL);
                        }
                        resultNode.getModel().setDescription(String.format("Pass: %s ; Fail: %s ; Skip: %s ;", passSize, failSize, skipSize));
                    }
                    buildTestNodes(resultNode, context.getFailedTests(), Status.FAIL);
                    buildTestNodes(resultNode, context.getSkippedTests(), Status.SKIP);
                    buildTestNodes(resultNode, context.getPassedTests(), Status.PASS);
                }
                if (suiteTest != null) {
                    suiteTest.getModel().setDescription(String.format("Pass: %s ; Fail: %s ; Skip: %s ;", suitePassSize, suiteFailSize, suiteSkipSize));
                    if (suiteFailSize > 0) {
                        suiteTest.getModel().setStatus(Status.FAIL);
                    }
                }
            }
            extent.flush();
            // 统计完成后清除suites集合，以免重复展示
            suites.clear();
        }
        // 暂时做输出展示
        Mongodb tools = new Mongodb.Builder().build().connectMongodb();
        String Id = tools.getIdByExample("name", projectName + "#" + ExtentManager.newInstance().countBuild(projectName));
        String reportURL = "http://10.200.173.92:1337/#/report-summary?id=" + Id;
        System.out.println();
        System.out.println("======================================================================================");
        System.out.println();
        System.out.println("报告地址：" + reportURL + "：报告地址");
        System.out.println();
        System.out.println("======================================================================================");
        System.out.println();
    }

    private void buildTestNodes(ExtentTest extentTest, IResultMap resultMap, Status status) {
        // 存在父节点时，获取父节点的标签
        String[] categories = new String[0];
        if (extentTest != null) {
            List<TestAttribute> categoryList = extentTest.getModel().getCategoryContext().getAll();
            categories = new String[categoryList.size()];
            for (int index = 0; index < categoryList.size(); index++) {
                categories[index] = categoryList.get(index).getName();
            }
        }
        ExtentTest test;
        if (resultMap.size() > 0) {
            // 调整用例排序，按时间排序
            Set<ITestResult> treeSet = new TreeSet<>(comparing(ITestResult::getEndMillis));
            treeSet.addAll(resultMap.getAllResults());
            // 记录的测试方法调用开始、结束时间
            List<Long> outPutTimes;
            for (ITestResult result : treeSet) {
                // 构造用例名称
                String name = createCaseName(result);
                // 获取记录的测试方法调用开始、结束时间
                outPutTimes = SaveRunTime.getOutput(result);
                if (extentTest == null) {
                    test = extent.createTest(name);
                    test.assignCategory(projectName);
                    test.assignAuthor(runAuthor);

                    if (outPutTimes.size() >= 2) {
                        test.getModel().setStartTime(getTime(outPutTimes.get(0)));
                        test.getModel().setEndTime(getTime(outPutTimes.get(1)));
                    }
                } else {
                    // 作为子节点进行创建时，设置同父节点的标签一致，便于报告检索。
                    test = extentTest.createNode(name).assignCategory(categories);

                    if (outPutTimes.size() >= 2) {
                        test.getModel().setStartTime(getTime(outPutTimes.get(0)));
                        test.getModel().setEndTime(getTime(outPutTimes.get(1)));
                    }
                }
                for (String group : result.getMethod().getGroups()) {
                    test.assignCategory(group);
                }
                List<String> outputList = Reporter.getOutput(result);
                // 将用例的log输出报告中
                outputList.forEach(test::warning);
                // 展示测试的入参和出参


                Map<String, Object> context = TestNGUtils.getParamContext(result);
                if (Boolean.FALSE.equals(CollectionUtils.isEmpty(context))) {
                    Gson gson = new Gson();
                    test.info("入参：" + gson.toJson(context.get(Constants.PARAMETER_NAME_)));

                    Object obj = context.get(Constants.RESULT_NAME);
                    if (obj instanceof Response) {
                        Response response = (Response) obj;
                        test.info("出参：" + response.asString());
                    } else {
                        test.info("出参：" + gson.toJson(obj));
                    }

                    String className = TestNGUtils.getTestCaseClassName(result);
                    Class<? extends Class> clazz = result.getTestClass().getRealClass().getClass();
                    String resource = clazz.getResource("").getPath();
                    String filePath = resource + className + ".xls";

                    ExcelResolver excel = new ExcelResolver(filePath,"exceptResult");
                    Map<String, Object> exceptResult = excel.readDataByRow(Integer.valueOf(context.get(Constants.CASE_INDEX).toString()));
                    test.info("断言内容：" + gson.toJson(exceptResult));
                }


                /*Map<String, String> map = getParamAndReturn(result);
                if (map != null) {
                    test.info("入参：" + map.get("param"));
                    test.info("出参：" + map.get("returnValue"));
                    if ((AnnotationUtils.isAutoAssert(TestNGUtils.getTestMethod(result)) && ParamUtils.isAutoAssert(context) &&
                            AnnotationUtils.getCheckMode(TestNGUtils.getTestMethod(result)) == CheckMode.NORMAL) || (!AnnotationUtils.isAutoAssert(TestNGUtils.getTestMethod(result))
                            || !ParamUtils.isAutoAssert(context))) {
                        if (map.get("expected_return") != null) {
                            test.info("预期返回值：" + map.get("expected_return"));
                        }
                    }
                    if (AnnotationUtils.isAutoAssert(TestNGUtils.getTestMethod(result)) && ParamUtils.isAutoAssert(context) &&
                            AnnotationUtils.getCheckMode(TestNGUtils.getTestMethod(result)) == CheckMode.REPLAY) {
                        String expectedReturn = AssertCheck.getExpectedReturn(HandleMethodName.getTestMethodName(result), map.get("param"));
                        if (expectedReturn != null) {
                            test.info("预期返回值：" + expectedReturn);
                        }
                    }
                }*/
                if (result.getThrowable() != null) {
                    test.log(status, result.getThrowable());
                } else {
                    test.log(status, "Test " + upperFirst(status.toString()) + "ed");
                }
            }
        }
    }

    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    /**
     * 把首字母转为大写
     * @param str
     * @return
     */
    private String upperFirst(String str) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(str)) {
            return str;
        }
        return str.replaceFirst(str.substring(0, 1), str.substring(0, 1).toUpperCase());
    }

    /**
     * 从zhubajie_qa_dubbo库中获取对应测试的入参和返回值
     * @param testResult 返回结果
     * @return 集合
     */
    private Map<String, String> getParamAndReturn(ITestResult testResult) {
        Map<String, Object> param = TestNGUtils.getParamContext(testResult);
        String className = HandleMethodName.getTestClassName(testResult);
        String methodName = HandleMethodName.getTestMethodName(testResult);
        String caseName;
        if (param.get(Constants.CASE_NAME) == null) {
            caseName = param.get(Constants.EXCEL_DESC).toString();
        } else {
            caseName = param.get(Constants.CASE_NAME).toString();
        }
        String sql = "SELECT * FROM autotest_result WHERE class_name=? AND methods_name=? AND case_name=? order by create_time desc";
        Object[] params = {className, methodName, caseName};
        AutoTestResult result = CIDbUtils.queryQaMethodValue(sql, params);
        Map<String, String> resultMap = Maps.newHashMap();
        if (result != null) {
            resultMap.put("param", result.getMethods_parameter());
            resultMap.put("returnValue", result.getMethods_return());
            if (!"".equals(result.getExpected_return()) && result.getExpected_return() != null) {
                resultMap.put("expected_return", result.getExpected_return());
            } else {
                resultMap.put("expected_return", null);
            }
            return resultMap;
        }
        return null;
    }

    /**
     * 创建报告用例标题名称
     * @param result 测试结果上下文
     * @return
     */
    private String createCaseName(ITestResult result) {
        String className = HandleMethodName.getTestClassName(result);
        String methodName = HandleMethodName.getTestMethodName(result);
        StringBuilder sb = new StringBuilder();

        Map<String, Object> param = null;
        try {
            param = TestNGUtils.getParamContext(result);
        } catch (Exception e) {
            System.out.println("---------------------" + className + "#" + methodName + "--------------------");
            e.printStackTrace();
        }

        if (Objects.nonNull(param) && Objects.nonNull(param.get(Constants.HTTP_METHOD))) {
            Object uri = param.get(Constants.HTTP_METHOD);
            sb.append(uri);
            sb.append(" --> ");
            sb.append("CaseName：");
        } else {
            sb.append(className);
            sb.append(".");
            sb.append(methodName);
            sb.append("() -> ");
            sb.append("CaseName：");
        }

        if (param != null) {
            if (param.get(Constants.CASE_NAME) == null) {
                sb.append(param.get(Constants.EXCEL_DESC).toString());
            } else {
                sb.append(param.get(Constants.CASE_NAME).toString());
            }
        }
        String name = sb.toString();
        if (sb.toString().length() > 0) {
            if (sb.toString().length() > 100) {
                name = sb.toString().substring(0, 99) + "...";
            }
        } else {
            name = result.getMethod().getMethodName();
        }
        return name;
    }
}
