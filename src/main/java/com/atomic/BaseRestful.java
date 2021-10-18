package com.atomic;

import cn.hutool.core.util.StrUtil;
import com.atomic.config.ConfigConstants;
import com.atomic.config.AtomicConfig;
import com.atomic.exception.ExceptionManager;
import com.atomic.exception.InjectResultException;
import com.atomic.param.Constants;
import com.atomic.param.ITestResultCallback;
import com.atomic.param.ResultCache;
import com.atomic.param.util.ParamUtils;
import com.atomic.tools.report.ReportListener;
import com.atomic.tools.report.SaveRunTime;
import com.atomic.tools.rollback.RollBackListener;
import com.atomic.util.GsonUtils;
import com.atomic.util.TestNGUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.util.CollectionUtils;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Listeners;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.atomic.annotations.AnnotationUtils.isIgnoreMethod;
import static com.atomic.annotations.AnnotationUtils.isScenario;
import static com.atomic.param.CallBack.paramAndResultCallBack;
import static com.atomic.param.Constants.*;
import static com.atomic.param.ResultCache.saveTestRequestInCache;
import static com.atomic.param.util.ParamUtils.*;
import static com.atomic.tools.assertcheck.AssertResult.assertResultForRest;
import static com.atomic.tools.report.HandleMethodName.getTestMethodName;
import static com.atomic.tools.report.ParamPrint.resultPrint;
import static com.atomic.util.TestNGUtils.injectResultAndParameters;
import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.config.SSLConfig.sslConfig;
import static java.nio.charset.Charset.defaultCharset;

/**
 * 神兽保佑 代码无BUG!
 * @author dreamyao
 * @version 1.0.0
 * @title REST风格接口测试基类
 * @date  2018/05/30 10:48
 */
@Listeners({RollBackListener.class, ReportListener.class})
public abstract class BaseRestful extends AbstractRest implements IHookable, ITestBase {

    @Override
    public void initDb() {

    }

    @AfterClass(alwaysRun = true)
    public void closeSqlTools() {

    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        // 有Ignore注解，就直接转测试代码
        if (isIgnoreMethod(TestNGUtils.getTestMethod(testResult))) {
            // 直接执行@Test注解的测试方法
            callBack.runTestMethod(testResult);
            return;
        }

        try {
            startTest(callBack, testResult);
        } catch (Exception e) {
            ExceptionManager.throwNewException(e);
        }
    }

    private void startTest(IHookCallBack callBack, ITestResult testResult) throws Exception {

        // 从testng上下文中获取测试入场
        Map<String, Object> context = TestNGUtils.getParamContext(testResult);

        //注入场景测试所需要的依赖方法的返回结果
        TestNGUtils.injectScenarioReturnResult(testResult, context);

        // 递归组合参数并转化为真实值
        Map<String, Object> newContext = ParamUtils.assemblyParamMap2RequestMap(testResult,
                this, context);

        // 接口调用之前，先执行测试用例的beforeTest方法中的内容
        beforeTest(newContext);

        // 检查Http接口测试入参必填字段
        ParamUtils.checkKeyWord(newContext);

        //记录方法调用开始时间
        SaveRunTime.startTestTime(testResult);

        // 执行接口调用，并得到响应 Response
        Response response = startRequest(testResult, newContext);

        //记录方法调用结束时间
        SaveRunTime.endTestTime(testResult);

        // 缓存入参和返回值
        ResultCache.saveTestResultInCache(response, testResult, newContext);

        // 处理得到的Response 并执行结果数据打印、结果断言
        handleResponse(response, testResult, callBack, newContext);
    }

    @SuppressWarnings("all")
    private Response startRequest(ITestResult testResult, Map<String, Object> newContext) {
        Headers headers;
        String httpHost;
        if (!isHttpHostNoNull(newContext)) {
            // 如果测试用例中设置了host 则使用测试用例配置的
            httpHost = AtomicConfig.getStr(ConfigConstants.HTTP_HOST);
        } else {
            // 否则获取全局配置的host
            httpHost = newContext.get(HTTP_HOST).toString();
        }
        RequestSpecification specification = given().baseUri(httpHost)
                // 字符编码设置
                .config(config().encoderConfig(
                        encoderConfig().defaultContentCharset(defaultCharset())))
                // SSL设置，实现对Https的支持
                .config(config().sslConfig(sslConfig().allowAllHostnames()));

        //设置ContentType
        setContentType(specification, newContext);

        // 获取header配置的优先级为，先从测试用例配置中获取，如果测试用例没有配置，则获取全局的配置，如果全局配置没有配置header信息
        // 否则不设置header信息
        if (isHttpHeaderNoNull(newContext) && StrUtil.isNotBlank(newContext.get(HTTP_HEADER).toString())) {
            // 从测试用例中获取header信息
            String header = newContext.get(HTTP_HEADER).toString();
            specification = setHeader(specification, header);
        } else {
            // 从全局配置文件 test.properties文件中获取全局header配置信息
            String header = AtomicConfig.getStr(ConfigConstants.HTTP_HEADER);
            // 如果从全局配置文件中获取到header信息，则使用全局配置到信息
            // 设置全局header配置信息
            specification = setHeader(specification, header);
        }

        // 执行Http接口请求调用
        return doRequest(testResult, specification, newContext);
    }

    @SuppressWarnings("all")
    private RequestSpecification setHeader(RequestSpecification specification, String header) {
        // 把Json字符串反序列化为Map<String,String>集合
        Map<String, String> headerMap = GsonUtils.getGson().fromJson(header,
                new TypeToken<Map<String, String>>() {
                }.getType());

        List<Header> headers = Lists.newArrayList();

        // 构造Header对象集合
        Set<Map.Entry<String, String>> entries = headerMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            Header head = new Header(entry.getKey(), entry.getValue());
            headers.add(head);
        }

        // 设置Header
        specification = specification.headers(new Headers(headers));
        return specification;
    }

    @SuppressWarnings("all")
    private Response doRequest(ITestResult testResult,
                               RequestSpecification specification,
                               Map<String, Object> newContext) {

        Response response;
        // 从测试用例中获取请求类型，如：POST、GET
        String httpMode = newContext.get(HTTP_MODE).toString();
        // 从测试用例中获取请求地址
        String uri = newContext.get(HTTP_METHOD).toString();

        // 复制一份否则操作时会影响到测试上下文中的context
        Map<String, Object> copyContext = Maps.newHashMap(newContext);

        // 去除excel中的描述字段，如果 header、host等字段，留下真正的入参数据字段
        Map<String, Object> parameters = ParamUtils.getParameters(copyContext);

        // 如果是GET请求
        if (Constants.HTTP_GET.equalsIgnoreCase(httpMode)) {
            // 如果有参数
            if (parameters.size() > 0) {
                // 移除用例编号
                parameters.remove(Constants.CASE_INDEX);
                // 设置入参
                response = specification.params(parameters).when().get(uri);
                newContext.put(Constants.PARAMETER_NAME_, parameters);

            } else {
                // 如果没有入参，则不设置入参
                response = specification.when().get(uri);
                newContext.put(Constants.PARAMETER_NAME_, "");
            }
        } else if (Constants.HTTP_POST.equalsIgnoreCase(httpMode) &&
                ParamUtils.isJsonContext(newContext)) {
            // POST Json请求
            if (parameters.containsKey("request")) {

                String request = parameters.get("request").toString();

                // 执行接口调用
                response = specification.body(request).when().post(uri);

                newContext.put(Constants.PARAMETER_NAME_, parameters.get("request"));

                parameters = GsonUtils.getGson().fromJson(request,
                        new TypeToken<Map<String, Objects>>() {}.getType());

            } else {

                // 移除用例编号
                parameters.remove(Constants.CASE_INDEX);
                // 执行接口调用
                response = specification.body(parameters).when().post(uri);

                // 把入参和返回结果存入context中方便后续打印输出、测试报告展示等操作
                newContext.put(Constants.PARAMETER_NAME_, parameters);

            }
        } else {

            // POST 表单请求

            // 移除用例编号
            parameters.remove(Constants.CASE_INDEX);

            if (Boolean.FALSE.equals(CollectionUtils.isEmpty(parameters))) {
                // POST 有参表单提交
                // 执行接口调用
                response = specification.params(parameters).when().post(uri);
                newContext.put(Constants.PARAMETER_NAME_, parameters);

            } else {
                // POST 无参表单提交
                // 执行接口调用
                response = specification.when().post(uri);
                newContext.put(Constants.PARAMETER_NAME_, "");
            }
        }

        // 如果是场景测试
        if (isScenario(TestNGUtils.getTestMethod(testResult))) {
            // 保存测试场景接口入参对象
            saveTestRequestInCache(parameters, testResult, newContext);
        }

        return response;
    }

    private void handleResponse(Response response,
                                ITestResult testResult,
                                IHookCallBack callBack,
                                Map<String, Object> context) throws Exception {

        // 打印测试结果
        resultPrint(getTestMethodName(testResult), response, context);
        //回调函数，为testCase方法传入，入参和返回结果
        ITestResultCallback callback = paramAndResultCallBack();
        // 自动断言
        assertResultForRest(response, testResult, this, context, callback);
        // 回调testng 有@Test注解的测试方法
        testCallBack(callBack, testResult);
    }

    @SuppressWarnings("unchecked")
    private void testCallBack(IHookCallBack callBack, ITestResult testResult) {
        // 取出整个测试过程中产生的测试数据以及入参
        Map<String, Object> context = (Map<String, Object>) testResult.getParameters()[0];
        try {
            // 注入参数和结果给添加了@Test注解的测试方法
            injectResultAndParameters(context, testResult, this);
        } catch (Exception e) {
            Reporter.log("为TestCase方法注入入参和返回结果异常！");
            throw new InjectResultException(e);
        }
        // 回调测试方法
        callBack.runTestMethod(testResult);
    }

    private void setContentType(RequestSpecification specification, Map<String, Object> context) {
        if (isContentTypeNoNull(context)) {
            specification.contentType(context.get(Constants.CONTENT_TYPE).toString());
        }
    }
}
