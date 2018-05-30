package com.atomic;

import com.alibaba.fastjson.JSON;
import com.atomic.annotations.AnnotationUtils;
import com.atomic.config.TestMethodMode;
import com.atomic.enums.AutoTestMode;
import com.atomic.enums.CheckMode;
import com.atomic.exception.MethodMetaException;
import com.atomic.exception.ParameterException;
import com.atomic.exception.ThrowException;
import com.atomic.listener.IntegrationTestRollBackListener;
import com.atomic.listener.ReportListener;
import com.atomic.listener.SaveResultListener;
import com.atomic.param.*;
import com.atomic.param.assertcheck.AssertCheckUtils;
import com.atomic.param.entity.MethodMeta;
import com.atomic.tools.mock.dto.MockData;
import com.atomic.tools.mock.helper.MockFileHelper;
import com.atomic.util.FileUtils;
import com.atomic.util.MapUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.atomic.AutoTest.generateAutoTestCases;
import static com.atomic.annotations.AnnotationUtils.getAutoTestMode;
import static com.atomic.annotations.AnnotationUtils.getCheckMode;
import static com.atomic.annotations.AnnotationUtils.isScenario;
import static com.atomic.exception.ExceptionUtils.isExceptionThrowsBySpecialMethod;
import static com.atomic.listener.SaveRunTime.endTestTime;
import static com.atomic.listener.SaveRunTime.startTestTime;
import static com.atomic.param.Constants.*;
import static com.atomic.param.MethodMetaUtils.getMethodMeta;
import static com.atomic.param.MethodMetaUtils.getTestMethod;
import static com.atomic.param.ParamPrint.resultPrint;
import static com.atomic.param.ParamUtils.*;
import static com.atomic.param.ResultAssert.assertResult;
import static com.atomic.param.ResultAssert.exceptionDeal;
import static com.atomic.param.ResultAssert.resultCallBack;
import static com.atomic.param.StringUtils.isExcelValueEmpty;
import static com.atomic.param.TestNGUtils.injectResultAndParameters;
import static com.atomic.param.assertcheck.AssertCheck.recMode;
import static com.atomic.param.assertcheck.AssertCheck.replayMode;
import static com.atomic.param.assertcheck.AssertCheckUtils.getDataBeforeTest;
import static com.atomic.tools.mock.data.MockContext.getContext;
import static com.atomic.util.ApplicationUtils.getBean;
import static com.atomic.util.SaveResultUtils.saveTestRequestInCache;
import static com.atomic.util.SaveResultUtils.saveTestResultInCache;
import static org.apache.commons.io.FileUtils.readFileToString;


/**
 * 通用持续集成测试工具类
 * @author dreamyao
 * @version 1.0
 */
@SqlConfig
@Listeners({SaveResultListener.class, ReportListener.class, IntegrationTestRollBackListener.class})
@TestExecutionListeners(listeners = {TransactionalTestExecutionListener.class, SqlScriptsTestExecutionListener.class})
public abstract class CommonTest<T> extends BaseUnitTest implements IDubboTest {

    @BeforeClass(alwaysRun = true)
    protected void beforeClass() throws Exception {
        // 增加用户自己mock的功能
        MockitoAnnotations.initMocks(this);
        // 加载内存数据库
        initEmbeddedDataSource();
    }

    public void initDb() {

    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        // 有Ignore注解，就直接转测试代码
        if (AnnotationUtils.isIgnoreMethod(getTestMethod(testResult))) {
            super.run(callBack, testResult);
            return;
        }
        if (testResult.getParameters() == null || testResult.getParameters().length == 0) {
            Reporter.log("------------------ 获取测试入参异常！------------------");
            throw new ParameterException("获取测试入参异常！");
        }
        // 获取param
        Map<String, Object> param = getParamContext(testResult);
        // 注入场景测试所需要的依赖方法的返回结果
        injectScenarioReturnResult(testResult, param);
        // 为mock注入caseIndex
        getContext().setCaseIndex((Integer) param.get(Constants.CASE_INDEX));
        if (getContext().getMode() == TestMethodMode.REC) {
            deleteData();
        }
        if (getContext().getMode() == TestMethodMode.REPLAY) {
            loadData();
        }
        try {
            // 自动化测试
            if (isAutoTest(param)) {
                autoTest(callBack, testResult);
            } else if (isMultiThreads(param)) {
                // 多线程测试
                multiThreadTest(param, callBack, testResult);
            } else {
                startRunTest(param, callBack, testResult);
            }
        } catch (Exception e) {
            handleException(e, param, callBack, testResult);
        }
    }

    /**
     * 是否需要多线程测试
     * @param param 入参
     * @return
     */
    private boolean isMultiThreads(Map<String, Object> param) {
        if (!isExcelValueEmpty(param.get(THREAD_COUNT)) && Integer.valueOf(param.get(THREAD_COUNT).toString()) > 1) {
            return true;
        } else {
            return false;
        }
    }

    private void autoTest(IHookCallBack callBack, ITestResult testResult) throws Exception {
        // 跳过自动化测试用例
        if (getAutoTestMode(getTestMethod(testResult)) == AutoTestMode.NONE) {
            System.out.println("-----------------------------自动化测试用例已跳过!-----------------------------");
            return;
        }
        long start = System.currentTimeMillis();
        List<Map<String, Object>> allTestCases = generateAutoTestCases(testResult, this);
        System.out.println("------------------------------------------自动化测试开始，测试用例数量：" + allTestCases.size() + "------------------------------------------");
        final Exception[] exception = {null};
        Map<String, List<Map<String, Object>>> exceptionMsgs = Maps.newHashMap();
        allTestCases.forEach(newParam -> {
            try {
                MapUtils.mergeMap(getParamContext(testResult), newParam);// 合并参数
                startRunTest(newParam, callBack, testResult);
            } catch (Exception e) {
                if (exception[0] == null) {
                    exception[0] = e;
                }
                // 这里不抛异常，全部自动测试跑一遍，暴露问题，跑完再处理
                AutoTest.handleException(e, exceptionMsgs, testResult, newParam);
            }
        });
        long end = System.currentTimeMillis();
        System.out.println("------------------------------------------自动化测试结束，耗时：" + (end - start) / 1000 + "s------------------------------------------");
        // 如果有异常则抛出，提醒测试未通过
        AutoTest.printExceptions(exception[0], exceptionMsgs);
    }

    @SuppressWarnings("unchecked")
    private void multiThreadTest(final Map<String, Object> param, final IHookCallBack callBack, final ITestResult testResult) throws Exception {
        int threadCount = Integer.valueOf(param.get(THREAD_COUNT).toString());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        // 构建完成服务
        CompletionService completionService = new ExecutorCompletionService(executor);
        System.out.println("-----------------------多线程开始执行！-----------------------");
        for (int i = 1; i <= threadCount; i++) {
            // 向线程池提交任务
            completionService.submit(() -> {
                startRunTest(param, callBack, testResult);
                return null;
            });
        }
        // 按照完成顺序,打印结果
        for (int i = 0; i < threadCount; i++) {
            // 取出并移除表示下一个已完成任务的 Future，如果目前不存在这样的任务，则等待。
            completionService.take().get();
        }
        // 所有任务已经完成,关闭线程池
        System.out.println("-----------------------多线程执行完毕！-----------------------");
        executor.shutdown();
    }

    private void startRunTest(Map<String, Object> param, IHookCallBack callBack, ITestResult testResult) throws Exception {
        initDb();
        // 先执行beforeTest方法
        beforeTest(param);
        // 自动断言前获取数据库数据，以及把入参的sql值赋值成真实的值
        getDataBeforeTest(param, this);
        // 调用方法
        commonTest(testResult);
        // 结果为 Y 才执行断言
        if (isExpectSuccess(param)) {
            execAssertMethod(param, callBack, testResult);
        }
    }

    private void handleException(Exception e, Map<String, Object> param, IHookCallBack callBack, ITestResult testResult) {
        // 期望结果为 Y 直接抛异常 ；beforeTest里面的异常也直接抛出
        if (isExpectSuccess(param) || isExceptionThrowsBySpecialMethod(e, "beforeTest")) {
            Reporter.log("[ExceptionUtils#handleException()]:{} ---> beforeTest方法执行异常！");
            ThrowException.throwNewException(e);
            throw new RuntimeException(e);
        }
        param.put(RESULT_NAME, exceptionDeal(e));
        try {
            MethodMeta methodMeta = getMethodMeta(param, testResult, this);
            resultPrint(methodMeta.getInterfaceMethod().getName(), param.get(RESULT_NAME), param, param.get(PARAMETER_NAME_));
        } catch (Exception e1) {
            throw new MethodMetaException(e1);
        }
    }

    private void execAssertMethod(Map<String, Object> param, IHookCallBack callBack, ITestResult testResult) throws Exception {
        // 自动断言
        AssertCheckUtils.assertCheck(this, param);
        // 注入参数和结果，param 会去掉系统使用的一些数据
        injectResultAndParameters(param, testResult, this);
        // 转断言
        super.run(callBack, testResult);
    }

    private void commonTest(ITestResult testResult) throws Exception {
        commonTest(testResult, (param, result, parameters) -> {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    param.put(PARAMETER_NAME_ + i, parameters[i]);
                }
            }
            param.put(RESULT_NAME, result);
        });
    }

    private void commonTest(final ITestResult testResult, final ITestResultCallback callback) throws Exception {
        final Map<String, Object> param = getParamContext(testResult);
        final MethodMeta methodMeta = getMethodMeta(param, testResult, this);
        // 先判断是不是foreach循环，不是就只执行一次
        Object value = param.get(methodMeta.getMultiTimeField());
        execMethodMulitTimes(value, paramValue -> prepareExecMethod(testResult, paramValue, param, methodMeta, callback));
    }

    private void execMethodMulitTimes(Object paramValue, ITestMethodMultiTimes testMethod) throws Exception {
        // 循环遍历
        StringUtils.ForEachClass forEachClass = StringUtils.getForEachClass(paramValue);
        if (forEachClass != null) {
            for (int i = forEachClass.getStart(); i <= forEachClass.getEnd(); i++) {
                testMethod.execTestMethod(String.valueOf(i));
            }
        } else {
            testMethod.execTestMethod(paramValue);
        }
    }

    private void prepareExecMethod(ITestResult testResult, Object paramValue, Map<String, Object> param, final MethodMeta methodMeta, ITestResultCallback callback) throws Exception {
        // 如果有新值，替换成新值
        Map<String, Object> newParam = Maps.newHashMap(param);
        if (paramValue != null) {
            newParam.put(methodMeta.getMultiTimeField(), paramValue);
        }
        // 构造入参
        final Object[] parameters = generateParametersNew(methodMeta, newParam);
        if ((!isAutoTest(param) && !AnnotationUtils.isAutoTest(getTestMethod(testResult))) && isScenario(getTestMethod(testResult))) {
            // 保存测试场景接口入参对象
            saveTestRequestInCache(parameters[0], testResult, param);
        }
        param.put(PARAMETER_NAME_, parameters);
        // 备注有可能有额外信息
        param.put(EXCEL_DESC, newParam.get(EXCEL_DESC));
        execMethod(testResult, methodMeta, param, callback, parameters);
    }

    private void execMethod(ITestResult testResult, MethodMeta methodMeta, Map<String, Object> param, ITestResultCallback callback, Object... parameters) throws Exception {
        Method method = methodMeta.getInterfaceMethod();
        Object interfaceObj = methodMeta.getInterfaceObj();
        startTestTime(testResult);//记录方法调用开始时间
        Object result = method.invoke(interfaceObj, parameters);
        endTestTime(testResult);//记录方法调用结束时间
        if (!isAutoTest(param) && !AnnotationUtils.isAutoTest(getTestMethod(testResult))) {
            // 实现测试方法名、入参、返回结果、入参、CASE_INDEX数据入库
            // saveScenarioTestData(parameters, result, null, param, getTestCaseClassName(testResult));
            saveTestResultInCache(result, testResult, param);
        }
        if (isAutoTest(param) && !AnnotationUtils.isPrintResult(methodMeta.getTestMethod())) {
            System.out.println(method.getName() + "-----------------------------测试用例执行完一个-----------------------------");
        } else {
            // parameters 可能被接口改变，打印出来看起来就像有问题
            resultPrint(method.getName(), result, param, parameters);
        }
        if (AnnotationUtils.isAutoAssert(MethodMetaUtils.getTestMethod(testResult)) && ParamUtils.isAutoAssert(param)) {
            if (getCheckMode(MethodMetaUtils.getTestMethod(testResult)) == CheckMode.REC) {
                recMode(parameters[0], result, methodMeta);
                System.out.println("-----------------------------执行智能化断言录制模式成功！-----------------------------");
                resultCallBack(result, param, callback, parameters);
            } else if (getCheckMode(MethodMetaUtils.getTestMethod(testResult)) == CheckMode.REPLAY) {
                replayMode(parameters[0], result, methodMeta);
                System.out.println("-----------------------------执行智能化断言回放模式成功！-----------------------------");
                resultCallBack(result, param, callback, parameters);
            } else {
                assertResult(result, methodMeta.getReturnType(), param, callback, parameters);
            }
        } else {
            assertResult(result, methodMeta.getReturnType(), param, callback, parameters);
        }
    }

    /**
     * 删除Mock数据
     */
    private void deleteData() {
        FileUtils.removeDir(MockFileHelper.getMockFile(getContext().getCaseIndex()));
    }

    /**
     * 保存Mock数据
     */
    private void loadData() {
        try {
            File file = new File(MockFileHelper.getMockFile(getContext().getCaseIndex()));
            String data = getFileString(file);
            Object o = JSON.parseObject(data, MockData.class);
            MockData mockData = (MockData) o;
            getContext().setMockData(mockData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileString(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                stringBuilder.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 初始化h2内存数据库
     */
    private void initEmbeddedDataSource() {
        String resource = this.getClass().getResource("").getPath();
        File[] files = new File(resource).listFiles();
        if (ArrayUtils.isEmpty(files)) {
            return;
        }
        for (File file : files) {
            String name = file.getName();
            // 格式 data-memberMasterDataSource.sql
            if (name.startsWith("data-") && name.endsWith(".sql")) {
                String dataSource = name.replace("data-", "").replace(".sql", "");
                try {
                    executeSql(dataSource, readFileToString(file, "utf-8"), true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * @param dataSourceName            数据源名称
     * @param sql                       SQL语句
     * @param isCheckEmbeddedDataSource true 表示 内存数据库 才能调用
     * @throws SQLException
     */
    private void executeSql(String dataSourceName, String sql, boolean isCheckEmbeddedDataSource) throws SQLException {
        DataSource dataSource = (DataSource) getBean(dataSourceName);
        // 不是内存数据库就不执行初始化
        if (isCheckEmbeddedDataSource && !(dataSource instanceof SimpleDriverDataSource && ((SimpleDriverDataSource) dataSource).getUrl().startsWith("jdbc:h2"))) {
            return;
        }
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.execute();
        ps.close();
        conn.close();
    }
}

