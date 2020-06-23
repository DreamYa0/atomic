package com.atomic;

import com.atomic.config.AtomicConfig;
import com.atomic.exception.ExceptionManager;
import com.atomic.exception.InjectResultException;
import com.atomic.param.Constants;
import com.atomic.param.ITestResultCallback;
import com.atomic.param.ParamUtils;
import com.atomic.tools.report.ReportListener;
import com.atomic.param.SaveResultCache;
import com.atomic.tools.report.SaveRunTime;
import com.atomic.tools.rollback.RollBackListener;
import com.atomic.tools.rollback.ScenarioRollBackListener;
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
import static com.atomic.param.Constants.HTTP_HEADER;
import static com.atomic.param.Constants.HTTP_HOST;
import static com.atomic.param.Constants.HTTP_METHOD;
import static com.atomic.param.Constants.HTTP_MODE;
import static com.atomic.param.Constants.LOGIN_URL;
import static com.atomic.param.ParamUtils.isContentTypeNoNull;
import static com.atomic.param.ParamUtils.isHttpHeaderNoNull;
import static com.atomic.param.ParamUtils.isHttpHostNoNull;
import static com.atomic.param.ParamUtils.isLoginUrlNoNull;
import static com.atomic.tools.assertcheck.ResultAssert.assertResultForRest;
import static com.atomic.tools.report.HandleMethodName.getTestMethodName;
import static com.atomic.tools.report.ParamPrint.resultPrint;
import static com.atomic.param.SaveResultCache.saveTestRequestInCache;
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
@Listeners({ScenarioRollBackListener.class, RollBackListener.class, ReportListener.class})
public abstract class BaseRestful extends AbstractRestTest implements IHookable, ITestBase {

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

        Map<String, Object> context = TestNGUtils.getParamContext(testResult);

        //注入场景测试所需要的依赖方法的返回结果
        TestNGUtils.injectScenarioReturnResult(testResult, context);

        // 递归组合参数并转化为真实值
        Map<String, Object> newContext = ParamUtils.assemblyParamMap2RequestMap(testResult,
                this, context);

        // 先执行beforeTest
        beforeTest(newContext);

        // 检查Http接口测试入参必填字段
        ParamUtils.checkKeyWord(newContext);

        //记录方法调用开始时间
        SaveRunTime.startTestTime(testResult);

        // 执行接口调用
        Response response = startRequest(testResult, newContext);

        //记录方法调用结束时间
        SaveRunTime.endTestTime(testResult);

        // 缓存入参和返回值
        SaveResultCache.saveTestResultInCache(response, testResult, newContext);

        execMethod(response, testResult, callBack, newContext);
    }

    @SuppressWarnings("unchecked")
    private Response startRequest(ITestResult testResult, Map<String, Object> newContext) {
        Headers headers;
        String httpHost;
        if (!isHttpHostNoNull(newContext)) {
            httpHost = AtomicConfig.newInstance().getHttpHost();
        } else {
            httpHost = newContext.get(HTTP_HOST).toString();
        }
        RequestSpecification specification = given().baseUri(httpHost)
                // 编码设置
                .config(config().encoderConfig(encoderConfig().defaultContentCharset(defaultCharset())))
                // SSL 设置
                .config(config().sslConfig(sslConfig().allowAllHostnames()));

        //设置ContentType
        setContentType(specification, newContext);

        if (isLoginUrlNoNull(newContext)) {
            // 调用快捷登录
            headers = given().get(newContext.get(LOGIN_URL).toString()).getHeaders();
            specification = specification.headers(headers);
        } else if (isHttpHeaderNoNull(newContext) && newContext.get(HTTP_HEADER) != null) {

            // 把Header json字符串反序列化为List<Header>
            String headerStr = newContext.get(HTTP_HEADER).toString();
            List<Map<String, String>> headerMapList = GsonUtils.getGson().fromJson(headerStr,
                    new TypeToken<List<Map<String, String>>>() {
            }.getType());

            List<Header> headerList = Lists.newArrayList();

            for (Map<String, String> header : headerMapList) {
                Set<Map.Entry<String, String>> entries = header.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    Header head = new Header(entry.getKey(), entry.getValue());
                    headerList.add(head);
                }
            }

            specification = specification.headers(new Headers(headerList));
        }
        return getResponse(testResult, specification, newContext);
    }

    private Response getResponse(ITestResult testResult,
                                 RequestSpecification specification,
                                 Map<String, Object> newContext) {

        Response response;
        String httpMode = newContext.get(HTTP_MODE).toString();
        String uri = newContext.get(HTTP_METHOD).toString();

        // 复制一份否则操作时会影响到测试上下文中的context
        Map<String, Object> copyContext = Maps.newHashMap(newContext);

        // 去除excel中的描述字段
        Map<String, Object> parameters = ParamUtils.getParameters(copyContext);

        if (Constants.HTTP_GET.equalsIgnoreCase(httpMode)) {
            if (parameters.size() > 0) {

                parameters.remove(Constants.CASE_INDEX);

                response = specification.params(parameters).when().get(uri);
                newContext.put(Constants.PARAMETER_NAME_, parameters);

            } else {
                response = specification.when().get(uri);
                newContext.put(Constants.PARAMETER_NAME_, "");
            }
        } else if (Constants.HTTP_POST.equalsIgnoreCase(httpMode) && isJsonContext(newContext)) {
            // POST Json请求
            if (parameters.keySet().contains("request")) {

                String request = parameters.get("request").toString();

                response = specification.body(request).when().post(uri);
                newContext.put(Constants.PARAMETER_NAME_, parameters.get("request"));

                parameters = GsonUtils.getGson().fromJson(request, new TypeToken<Map<String, Objects>>() {}.getType());

            } else {

                // 构造出真正的入参
                parameters.remove(Constants.CASE_INDEX);

                response = specification.body(parameters).when().post(uri);

                // 把入参和返回结果存入context中方便后续打印输出、测试报告展示等操作
                newContext.put(Constants.PARAMETER_NAME_, parameters);

            }
        } else {

            parameters.remove(Constants.CASE_INDEX);

            if (Boolean.FALSE.equals(CollectionUtils.isEmpty(parameters))) {

                // POST 有参表单提交
                response = specification.params(parameters).when().post(uri);
                newContext.put(Constants.PARAMETER_NAME_, parameters);

            } else {
                // POST 无参表单提交
                response = specification.when().post(uri);
                newContext.put(Constants.PARAMETER_NAME_, "");
            }
        }

        if (isScenario(TestNGUtils.getTestMethod(testResult))) {
            // 保存测试场景接口入参对象
            saveTestRequestInCache(parameters, testResult, newContext);
        }

        return response;
    }

    private boolean isJsonContext(Map<String, Object> context) {
        // 判断 ContentType 是否为 application/json
        return Objects.nonNull(context.get(Constants.CONTENT_TYPE)) &&
                Constants.CONTENT_TYPE_JSON.equalsIgnoreCase(context.get(Constants.CONTENT_TYPE).toString());
    }

    private void execMethod(Response response,
                            ITestResult testResult,
                            IHookCallBack callBack,
                            Map<String, Object> context) throws Exception {

        String result = response.body().asString();
        // 打印测试结果
        resultPrint(getTestMethodName(testResult), response, context);
        //回调函数，为testCase方法传入，入参和返回结果
        ITestResultCallback callback = paramAndResultCallBack();
        // 自动断言
        assertResultForRest(response, testResult, this, context, callback);
        testCallBack(callBack, testResult);
    }

    @SuppressWarnings("unchecked")
    private void testCallBack(IHookCallBack callBack, ITestResult testResult) {
        // 测试方法回调
        Map<String, Object> context = (Map<String, Object>) testResult.getParameters()[0];
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

    private void setContentType(RequestSpecification specification, Map<String, Object> context) {
        if (isContentTypeNoNull(context)) {
            specification.contentType(context.get(Constants.CONTENT_TYPE).toString());
        }
    }
}
