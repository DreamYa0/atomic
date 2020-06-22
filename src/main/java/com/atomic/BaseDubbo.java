package com.atomic;

import com.atomic.annotations.AnnotationUtils;
import com.atomic.config.GlobalConfig;
import com.atomic.enums.AutoTestMode;
import com.atomic.exception.InjectResultException;
import com.atomic.exception.InvokeException;
import com.atomic.exception.ParameterException;
import com.atomic.report.ReportListener;
import com.atomic.rollback.RollBackListener;
import com.atomic.rollback.ScenarioRollBackListener;
import com.atomic.param.AutoTest;
import com.atomic.param.HandleMethodName;
import com.atomic.param.ITestMethodMultiTimes;
import com.atomic.param.ITestResultCallback;
import com.atomic.param.MethodMetaUtils;
import com.atomic.param.ParamPrint;
import com.atomic.param.ParamUtils;
import com.atomic.param.StringUtils;
import com.atomic.param.entity.MethodMeta;
import com.atomic.tools.dubbo.DubboServiceFactory;
import com.atomic.util.MapUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Listeners;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.atomic.annotations.AnnotationUtils.getAutoTestMode;
import static com.atomic.annotations.AnnotationUtils.getServiceVersion;
import static com.atomic.annotations.AnnotationUtils.isAutoTest;
import static com.atomic.annotations.AnnotationUtils.isIgnoreMethod;
import static com.atomic.annotations.AnnotationUtils.isScenario;
import static com.atomic.annotations.AnnotationUtils.isServiceVersion;
import static com.atomic.exception.ExceptionUtils.handleException;
import static com.atomic.report.SaveRunTime.endTestTime;
import static com.atomic.report.SaveRunTime.startTestTime;
import static com.atomic.param.AutoTest.generateAutoTestCases;
import static com.atomic.param.AutoTest.printExceptions;
import static com.atomic.param.CallBack.paramAndResultCallBack;
import static com.atomic.param.Constants.CASE_NAME;
import static com.atomic.param.Constants.PARAMETER_NAME_;
import static com.atomic.param.ExcelParamConverter.getDataBeforeTest;
import static com.atomic.param.ResultAssert.assertResult;
import static com.atomic.param.TestNGUtils.getParamContext;
import static com.atomic.param.TestNGUtils.getTestMethod;
import static com.atomic.param.TestNGUtils.injectResultAndParameters;
import static com.atomic.param.TestNGUtils.injectScenarioReturnResult;
import static com.atomic.report.SaveResultCache.saveTestRequestInCache;
import static com.atomic.report.SaveResultCache.saveTestResultInCache;


/**
 * ┌─┐       ┌─┐
 * ┌──┘ ┴───────┘ ┴──┐
 * │                 │
 * │       ───       │
 * │  ─┬┘       └┬─  │
 * │                 │
 * │       ─┴─       │
 * │                 │
 * └───┐         ┌───┘
 * │         │
 * │         │ 神兽保佑
 * │         │代码无BUG!
 * │         └──────────────┐
 * │                        │
 * │                        ├─┐
 * │                        ┌─┘
 * │                        │
 * └─┐  ┐  ┌───────┬──┐  ┌──┘
 * │ ─┤ ─┤       │ ─┤ ─┤
 * └──┴──┘       └──┴──┘
 * @author dreamyao
 * @version 2.0.0 Created by dreamyao on 2017/5/9.
 * @title dubbo接口测试基类
 */
@Listeners({ScenarioRollBackListener.class, RollBackListener.class, ReportListener.class})
public abstract class BaseDubbo<T> extends AbstractDubboTest implements IHookable, ITestBase {

    protected final DubboServiceFactory dubboServiceFactory = new DubboServiceFactory();

    /**
     * db初始化,此方法中初始化的数据无法自动回滚
     */
    @Override
    public void initDb() {

    }

    @AfterClass(alwaysRun = true)
    public void closeSqlTools() {

    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        // 有Ignore注解，就直接转测试代码
        if (isIgnoreMethod(getTestMethod(testResult))) {
            callBack.runTestMethod(testResult);
            return;
        }
        Class<? extends T> clazz = null;
        Map<String, Object> context = null;
        String testMethodName = null;
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            String dubboServiceVersion = null;
            if (GlobalConfig.getServiceVersion() != null) {
                dubboServiceVersion = GlobalConfig.getServiceVersion();
            } else if (isServiceVersion(getTestMethod(testResult))) {
                dubboServiceVersion = getServiceVersion(getTestMethod(testResult));
            }
            // 获取被测接口定义
            clazz = MethodMetaUtils.getInterfaceClass(this);
            Class<? extends T> finalClazz = clazz;
            String finalDubboServiceVersion = dubboServiceVersion;
            // 异步加载
            future = CompletableFuture.supplyAsync(() -> dubboServiceFactory.getService(finalClazz,
                    finalDubboServiceVersion));

            if (testResult.getParameters() == null || testResult.getParameters().length == 0) {
                Reporter.log("获取测试入参异常！");
                throw new ParameterException("获取测试入参异常！");
            }
            // 获取测试入参
            context = getParamContext(testResult);
            // 注入场景测试所需要的依赖方法的返回结果
            injectScenarioReturnResult(testResult, context);
            // 调用接口里的被测方法名
            testMethodName = HandleMethodName.getTestMethodName(testResult);
            if (ParamUtils.isAutoTest(context) && isAutoTest(getTestMethod(testResult))) {
                autoTest(testResult, clazz, future, testMethodName);
            } else {
                startRunTest(testResult, clazz, context, testMethodName, future);
            }
            // 测试方法回调
            testCallBack(callBack, testResult);
        } catch (Exception e) {
            handleException(testResult, clazz, testMethodName, context, e, future);
        }
    }

    /**
     * 自动化测试
     * @param testResult     测试返回内容视图
     * @param clazz          接口class对象
     * @param future         接口实例
     * @param testMethodName 被测方法名
     * @throws Exception
     */
    private void autoTest(ITestResult testResult,
                          Class<? extends T> clazz,
                          CompletableFuture<Object> future,
                          String testMethodName) throws Exception {

        // 跳过自动化测试用例
        if (getAutoTestMode(getTestMethod(testResult)) == AutoTestMode.NONE) {
            System.out.println("-------------------自动化测试用例已跳过-------------------");
            return;
        }
        long start = System.currentTimeMillis();
        List<Map<String, Object>> allTestCases = generateAutoTestCases(testResult, clazz, testMethodName, future);
        final Exception[] exception = {null};
        Map<String, List<Map<String, Object>>> exceptionMsgs = Maps.newHashMap();
        Map<String, Object> paramMap = getParamContext(testResult);
        List<String> removeKeys = Lists.newArrayList();
        paramMap.forEach((key, value) -> {
            if (!"".equals(value) && value != null) {
                removeKeys.add(key);
            }
        });
        allTestCases.forEach(newParam -> removeValueByKeys(newParam, removeKeys));
        // 去除List中重复的Map
        toHeavy4ListMap(allTestCases);
        System.out.println("----------------------------------------自动化测试开始，测试用例数量：" +
                allTestCases.size() + "----------------------------------------");
        allTestCases.forEach(Param -> {
            // 合并参数
            MapUtils.mergeMap(paramMap, Param);
            try {
                startRunTest(testResult, clazz, paramMap, testMethodName, future);
            } catch (Exception e) {
                if (exception[0] == null) {
                    exception[0] = e;
                }
                // 这里不抛异常，全部自动测试跑一遍，暴露问题，跑完再处理
                AutoTest.handleException(e, exceptionMsgs, testResult, paramMap);
            }
        });
        long end = System.currentTimeMillis();
        System.out.println("----------------------------------------自动化测试结束，耗时：" +
                (end - start) / 1000 + "s----------------------------------------");
        // 如果有异常则抛出，提醒测试未通过
        printExceptions(exception[0], exceptionMsgs);
    }

    /**
     * 去除List中重复的Map,此方法效率有点低，后续重新实现
     * @param allTestCases 待去重待 List<Map<String, Object>> 结合
     */
    private void toHeavy4ListMap(List<Map<String, Object>> allTestCases) {
        for (int i = 0; i < allTestCases.size(); i++) {
            for (int j = i + 1; j < allTestCases.size(); j++) {
                boolean isNoEqual = false;
                for (String key : allTestCases.get(i).keySet()) {
                    if (!allTestCases.get(i).get(key).equals(allTestCases.get(j).get(key))) {
                        isNoEqual = true;
                        break;
                    }
                }
                if (!isNoEqual) {
                    allTestCases.remove(j);
                    toHeavy4ListMap(allTestCases);
                }
            }
        }
    }

    /**
     * 根据key集合移除值
     * @param map  map集合
     * @param keys keys
     */
    private void removeValueByKeys(Map<String, Object> map, List<String> keys) {
        keys.forEach(map::remove);
    }

    /**
     * 运行测试
     * @param clazz          接口class对象
     * @param context        入参
     * @param testMethodName 测试方法名称
     * @throws Exception
     */
    private void startRunTest(ITestResult testResult,
                              Class<? extends T> clazz,
                              Map<String, Object> context,
                              String testMethodName,
                              CompletableFuture<Object> future) throws Exception {

        initDb();
        // 先执行beforeTest
        beforeTest(context);
        // 如果excel中字段的值为SQL则把SQL语句变为对应的真实值
        getDataBeforeTest(context);
        // 获取测试所需要的属性
        MethodMeta methodMeta = MethodMetaUtils.getMethodMeta(testResult, clazz, testMethodName, context, future);
        // execMethod(parameters,methodMeta, context);
        runForEachTest(testResult, context, methodMeta);
    }

    /**
     * 解析excel中foreach关键字
     * @param context
     * @param methodMeta
     * @throws Exception
     */
    private void runForEachTest(final ITestResult testResult,
                                final Map<String, Object> context,
                                final MethodMeta methodMeta) throws Exception {

        // 先判断是不是foreach循环，不是就只执行一次
        Object param = context.get(methodMeta.getMultiTimeField());
        execMethodMulitTimes(param, paramValue -> prepareExecMethod(testResult, paramValue, context, methodMeta));
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

    private void prepareExecMethod(ITestResult testResult,
                                   Object paramValue,
                                   Map<String, Object> param,
                                   final MethodMeta methodMeta) throws Exception {

        // 如果有新值，替换成新值
        Map<String, Object> newParam = Maps.newHashMap(param);
        if (paramValue != null) {
            newParam.put(methodMeta.getMultiTimeField(), paramValue);
        }

        //构造入参
        final Object[] parameters = ParamUtils.generateParametersNew(methodMeta, newParam);

        if ((!ParamUtils.isAutoTest(param) && !isAutoTest(getTestMethod(testResult))) &&
                isScenario(getTestMethod(testResult))) {

            // 保存测试场景接口入参对象
            saveTestRequestInCache(parameters[0], testResult, param);
        }
        param.put(PARAMETER_NAME_, parameters);
        param.put(CASE_NAME, newParam.get(CASE_NAME));// 备注有可能有额外信息
        execMethod(testResult, parameters, methodMeta, param);
    }

    /**
     * 调用测试方法、出入参数打印、结果断言
     * @param parameters 入参
     * @param methodMeta 测试所需要的属性
     * @param context    入参
     * @throws Exception
     */
    private void execMethod(ITestResult testResult,
                            final Object[] parameters,
                            MethodMeta methodMeta,
                            Map<String, Object> context) throws Exception {

        //记录方法调用开始时间
        startTestTime(testResult);
        //通过反射调用方法
        Object result = methodMeta.getInterfaceMethod().invoke(methodMeta.getInterfaceObj(), parameters);
        //记录方法调用结束时间
        endTestTime(testResult);

        if (result == null) {
            throw new InvokeException("调用测试方法返回结果为null！");
        }

        if (!ParamUtils.isAutoTest(context) && !isAutoTest(getTestMethod(testResult))) {
            //实现测试方法名、入参、返回结果、入参、CASE_INDEX数据入库
            // saveScenarioTestData(parameters, result, null, context, getTestCaseClassName(testResult));
            saveTestResultInCache(result, testResult, context);
        }
        boolean isPrintResult = AnnotationUtils.isPrintResult(methodMeta.getTestMethod());
        if (ParamUtils.isAutoTest(context) && !isPrintResult) {
            System.out.println(methodMeta.getInterfaceMethod().getName() + "-----------------测试用例执行完一个！" +
                    "-----------------");
        } else {
            ParamPrint.resultPrint(methodMeta.getMethodName(), result, context, parameters);
        }
        /*if (AnnotationUtils.isAutoAssert(getTestMethod(testResult)) && ParamUtils.isAutoAssert(context)) {
            if (getCheckMode(getTestMethod(testResult)) == CheckMode.REC) {
                resultCallBack(result, context, callback);
                recMode(parameters[0], result, methodMeta);
                System.out.println("-----------------------------执行智能化断言录制模式成功！-----------------------------");
            } else if (getCheckMode(getTestMethod(testResult)) == CheckMode.REPLAY) {
                resultCallBack(result, context, callback);
                replayMode(parameters[0], result, methodMeta);
                System.out.println("-----------------------------执行智能化断言回放模式成功！-----------------------------");
            } else {
                assertResult(result, testResult, this, context, callback, parameters);
            }
        } else {
            assertResult(result, testResult, this, context, callback, parameters);
        }*/

        //回调函数，为testCase方法传入，入参和返回结果
        ITestResultCallback callback = paramAndResultCallBack();
        assertResult(result, testResult, this, context, callback, parameters);
    }

    /**
     * 测试方法回调
     * @param callBack
     * @param testResult
     */
    @SuppressWarnings("unchecked")
    private void testCallBack(IHookCallBack callBack, ITestResult testResult) {
        try {
            Map<String, Object> context = getParamContext(testResult);
            if (ParamUtils.isExpectSuccess(context)) {
                try {
                    // 注入参数和结果，param 会去掉系统使用的一些数据
                    injectResultAndParameters(context, testResult, this);
                } catch (Exception e) {
                    Reporter.log("为TestCase方法注入入参和返回结果异常！");
                    throw new InjectResultException(e);
                }
                //回调测试方法
                callBack.runTestMethod(testResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
