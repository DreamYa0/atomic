package com.atomic;

import com.atomic.annotations.AnnotationUtils;
import com.atomic.exception.ExceptionManager;
import com.atomic.exception.MethodMetaException;
import com.atomic.exception.ParameterException;
import com.atomic.param.Constants;
import com.atomic.param.ITestMethodMultiTimes;
import com.atomic.param.ITestResultCallback;
import com.atomic.param.entity.MethodMeta;
import com.atomic.param.util.ObjUtils;
import com.atomic.param.util.ParamUtils;
import com.atomic.tools.assertcheck.AssertCheckUtils;
import com.atomic.tools.autotest.AutoTestMode;
import com.atomic.tools.mock.data.TestMethodMode;
import com.atomic.tools.mock.helper.MockFileHelper;
import com.atomic.tools.report.ReportListener;
import com.atomic.tools.rollback.RollBackListener;
import com.atomic.util.TestNGUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import mockit.Capturing;
import org.apache.commons.lang3.ArrayUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.File;
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

import static com.atomic.annotations.AnnotationUtils.getAutoTestMode;
import static com.atomic.annotations.AnnotationUtils.isScenario;
import static com.atomic.param.Constants.*;
import static com.atomic.param.ResultCache.saveTestRequestInCache;
import static com.atomic.param.ResultCache.saveTestResultInCache;
import static com.atomic.param.entity.MethodMetaUtils.getMethodMeta;
import static com.atomic.param.util.ParamUtils.isAutoTest;
import static com.atomic.param.util.ParamUtils.isExpectSuccess;
import static com.atomic.tools.assertcheck.AssertResult.assertResult;
import static com.atomic.tools.autotest.AutoTestManager.generateAutoTestCases;
import static com.atomic.tools.mock.data.MockContext.getContext;
import static com.atomic.tools.report.ParamPrint.resultPrint;
import static com.atomic.tools.report.SaveRunTime.endTestTime;
import static com.atomic.tools.report.SaveRunTime.startTestTime;
import static com.atomic.util.ApplicationUtils.getBean;
import static com.atomic.util.TestNGUtils.injectResultAndParameters;


/**
 * 通用持续集成测试工具类
 * @author dreamyao
 * @version 1.0
 */
@SqlConfig
@Listeners({ReportListener.class, RollBackListener.class})
@TestExecutionListeners(listeners = {TransactionalTestExecutionListener.class, SqlScriptsTestExecutionListener.class})
public abstract class CommonTest<T> extends AbstractUnit implements ITestBase {

    @Capturing
    protected HttpSession session;
    @Capturing
    protected HttpServletRequest request;
    @Capturing
    protected HttpServletResponse response;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 4);

    @BeforeClass(alwaysRun = true)
    protected void beforeClass() throws Exception {
        // 加载内存数据库
        initEmbeddedDataSource();
    }

    @Override
    public void initDb() {

    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        // 有Ignore注解，就直接转测试代码
        if (AnnotationUtils.isIgnoreMethod(TestNGUtils.getTestMethod(testResult))) {
            super.run(callBack, testResult);
            return;
        }

        if (testResult.getParameters() == null || testResult.getParameters().length == 0) {
            Reporter.log("------------------ 获取测试入参异常！------------------");
            throw new ParameterException("获取测试入参异常！");
        }

        // 获取param
        Map<String, Object> context = TestNGUtils.getParamContext(testResult);
        // 注入场景测试所需要的依赖方法的返回结果
        TestNGUtils.injectScenarioReturnResult(testResult, context);
        // 为mock注入caseIndex
        getContext().setCaseIndex((Integer) context.get(Constants.CASE_INDEX));

        // 录制模式
        if (getContext().getMode() == TestMethodMode.REC) {
            MockFileHelper.deleteData();
        }

        // 回放模式
        if (getContext().getMode() == TestMethodMode.REPLAY) {
            MockFileHelper.loadData();
        }

        try {

            if (isAutoTest(context)) {
                // 自动化测试
                autoTest(callBack, testResult);
            } else if (isMultiThreads(context)) {
                // 多线程测试
                multiThreadTest(context, callBack, testResult);
            } else {
                // 普通测试
                startRunTest(context, callBack, testResult);
            }
        } catch (Exception e) {
            handleException(e, context, callBack, testResult);
        }
    }

    private boolean isMultiThreads(Map<String, Object> context) {
        // 是否需要多线程测试
        return !ParamUtils.isExcelValueEmpty(context.get(THREAD_COUNT)) &&
                Integer.parseInt(context.get(THREAD_COUNT).toString()) > 1;
    }

    private void autoTest(IHookCallBack callBack, ITestResult testResult) throws Exception {
        // 跳过自动化测试用例
        if (getAutoTestMode(TestNGUtils.getTestMethod(testResult)) == AutoTestMode.NONE) {
            System.out.println("------------------- 自动化测试用例已跳过! -------------------");
            return;
        }
        long start = System.currentTimeMillis();
        List<Map<String, Object>> allTestCases = generateAutoTestCases(testResult, this);
        System.out.println("------------------- 自动化测试开始，测试用例数量："
                + allTestCases.size() + " -------------------");
        final Exception[] exception = {null};
        Map<String, List<Map<String, Object>>> exceptionMsgs = Maps.newHashMap();
        allTestCases.forEach(newParam -> {
            try {
                // 合并参数
                TestNGUtils.getParamContext(testResult).putAll(newParam);
                startRunTest(newParam, callBack, testResult);
            } catch (Exception e) {
                if (exception[0] == null) {
                    exception[0] = e;
                }
                // 这里不抛异常，全部自动测试跑一遍，暴露问题，跑完再处理
                ExceptionManager.handleException(e, exceptionMsgs, testResult, newParam);
            }
        });
        long end = System.currentTimeMillis();
        System.out.println(" -------------------自动化测试结束，耗时："
                + (end - start) / 1000 + "s -------------------");
        // 如果有异常则抛出，提醒测试未通过
        ExceptionManager.printExceptions(exception[0], exceptionMsgs);
    }

    @SuppressWarnings("all")
    private void multiThreadTest(final Map<String, Object> context,
                                 final IHookCallBack callBack,
                                 final ITestResult testResult) throws Exception {

        int threadCount = Integer.parseInt(context.get(THREAD_COUNT).toString());
        // 构建完成服务
        CompletionService completionService = new ExecutorCompletionService(EXECUTOR);
        System.out.println("------------------- 多线程开始执行！-------------------");
        for (int i = 1; i <= threadCount; i++) {
            // 向线程池提交任务
            completionService.submit(() -> {
                startRunTest(context, callBack, testResult);
                return null;
            });
        }
        // 按照完成顺序,打印结果
        for (int i = 0; i < threadCount; i++) {
            // 取出并移除表示下一个已完成任务的 Future，如果目前不存在这样的任务，则等待。
            completionService.take().get();
        }
        // 所有任务已经完成,关闭线程池
        System.out.println("------------------- 多线程执行完毕！-------------------");
        EXECUTOR.shutdown();
    }

    private void startRunTest(Map<String, Object> context,
                              IHookCallBack callBack,
                              ITestResult testResult) throws Exception {
        // 初始化DB
        initDb();

        // 先执行beforeTest方法
        beforeTest(context);

        // 自动断言前获取数据库数据，以及把入参的sql值赋值成真实的值
        ParamUtils.getDataBeforeTest(context, this);

        // 调用方法
        commonTest(testResult);

        // 结果为 Y 才执行断言
        if (isExpectSuccess(context)) {
            execAssertMethod(context, callBack, testResult);
        }
    }

    private void handleException(Exception e,
                                 Map<String, Object> context,
                                 IHookCallBack callBack,
                                 ITestResult testResult) {

        // 期望结果为 Y 直接抛异常 ；beforeTest里面的异常也直接抛出
        if (isExpectSuccess(context) || ExceptionManager.isExceptionThrowsBySpecialMethod(e, "beforeTest")) {
            Reporter.log("beforeTest方法执行异常！");
            ExceptionManager.throwNewException(e);
            throw new RuntimeException(e);
        }
        context.put(RESULT_NAME, ExceptionManager.exceptionDeal(e));
        try {
            MethodMeta methodMeta = getMethodMeta(context, testResult, this);
            resultPrint(methodMeta.getInterfaceMethod().getName(), context.get(RESULT_NAME), context,
                    context.get(PARAMETER_NAME_));
        } catch (Exception e1) {
            throw new MethodMetaException(e1);
        }
    }

    private void execAssertMethod(Map<String, Object> context,
                                  IHookCallBack callBack,
                                  ITestResult testResult) throws Exception {
        // 自动断言
        AssertCheckUtils.assertCheck(this, context);
        // 注入参数和结果，context 会去掉系统使用的一些数据
        injectResultAndParameters(context, testResult, this);
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
        final Map<String, Object> context = TestNGUtils.getParamContext(testResult);
        final MethodMeta methodMeta = getMethodMeta(context, testResult, this);
        // 先判断是不是foreach循环，不是就只执行一次
        Object value = context.get(methodMeta.getMultiTimeField());
        execMethodMulitTimes(value, paramValue -> prepareExecMethod(testResult, paramValue, context,
                methodMeta, callback));
    }

    private void execMethodMulitTimes(Object paramValue, ITestMethodMultiTimes testMethod) throws Exception {
        // 循环遍历
        ObjUtils.ForEachClass forEachClass = ObjUtils.getForEachClass(paramValue);
        if (forEachClass != null) {
            for (int i = forEachClass.getStart(); i <= forEachClass.getEnd(); i++) {
                testMethod.execTestMethod(String.valueOf(i));
            }
        } else {
            testMethod.execTestMethod(paramValue);
        }
    }

    private void prepareExecMethod(ITestResult testResult,
                                   Object paramValue,
                                   Map<String, Object> context,
                                   final MethodMeta methodMeta,
                                   ITestResultCallback callback) throws Exception {

        // 如果有新值，替换成新值
        Map<String, Object> newParam = Maps.newHashMap(context);
        if (paramValue != null) {
            newParam.put(methodMeta.getMultiTimeField(), paramValue);
        }

        // 设置mock对象
        newParam.put(Constants.HTTP_SESSION, session);
        newParam.put(Constants.HTTP_SERVLET_REQUEST, request);
        newParam.put(Constants.HTTP_SERVLET_RESPONSE, response);

        // 构造请求入参对象
        final Object[] parameters = ObjUtils.generateParametersNew(methodMeta, newParam);

        if ((!isAutoTest(context) && !AnnotationUtils.isAutoTest(TestNGUtils.getTestMethod(testResult))) &&
                isScenario(TestNGUtils.getTestMethod(testResult))) {
            // 保存测试场景接口入参对象
            saveTestRequestInCache(parameters[0], testResult, context);
        }

        // 备注有可能有额外信息
        context.put(EXCEL_DESC, newParam.get(EXCEL_DESC));

        // 把入参和返回结果存入context中方便后续打印输出、测试报告展示等操作
        context.put(Constants.PARAMETER_NAME_, parameters);

        execMethod(testResult, methodMeta, context, callback, parameters);
    }

    private void execMethod(ITestResult testResult,
                            MethodMeta methodMeta,
                            Map<String, Object> context,
                            ITestResultCallback callback,
                            Object... parameters) throws Exception {

        Method method = methodMeta.getInterfaceMethod();
        Object interfaceObj = methodMeta.getInterfaceObj();

        // 记录方法调用开始时间
        startTestTime(testResult);

        // 调用方法
        Object result = method.invoke(interfaceObj, parameters);

        // 记录方法调用结束时间
        endTestTime(testResult);

        if (!isAutoTest(context) && !AnnotationUtils.isAutoTest(TestNGUtils.getTestMethod(testResult))) {
            // 实现测试方法名、入参、返回结果、入参、CASE_INDEX数据入库
            // saveScenarioTestData(parameters, result, null, context, getTestCaseClassName(testResult));
            saveTestResultInCache(result, testResult, context);
        }

        if (isAutoTest(context) && !AnnotationUtils.isPrintResult(methodMeta.getTestMethod())) {
            System.out.println(method.getName() + "------------------- 测试用例执行完一个 " +
                    " -------------------");
        } else {
            // parameters 可能被接口改变，打印出来看起来就像有问题
            resultPrint(method.getName(), result, context, parameters);
        }

        assertResult(result, testResult, this, context, callback, parameters);
    }

    private void initEmbeddedDataSource() {
        // 初始化h2内存数据库
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
                    executeSql(dataSource, Files.asCharSource(file, Charsets.UTF_8).read(),
                            true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void executeSql(String dataSourceName, String sql, boolean isCheckEmbeddedDataSource) throws SQLException {
        DataSource dataSource = (DataSource) getBean(dataSourceName);
        // 不是内存数据库就不执行初始化
        if (isCheckEmbeddedDataSource && !(dataSource instanceof SimpleDriverDataSource &&
                ((SimpleDriverDataSource) dataSource).getUrl().startsWith("jdbc:h2"))) {
            return;
        }
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.execute();
        ps.close();
        conn.close();
    }
}

