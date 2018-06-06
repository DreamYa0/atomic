package com.atomic;

import com.atomic.annotations.AnnotationUtils;
import com.atomic.config.CenterConfig;
import com.atomic.enums.CheckMode;
import com.atomic.exception.InjectResultException;
import com.atomic.listener.RollBackListener;
import com.atomic.listener.SaveRunTime;
import com.atomic.listener.ScenarioRollBackListener;
import com.atomic.param.Constants;
import com.atomic.param.HandleExcelParam;
import com.atomic.param.ITestResultCallback;
import com.atomic.param.ParamUtils;
import com.atomic.param.TestNGUtils;
import com.atomic.tools.sql.NewSqlTools;
import com.atomic.tools.sql.SqlTools;
import com.atomic.util.SaveResultUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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

import static com.atomic.annotations.AnnotationUtils.getCheckMode;
import static com.atomic.annotations.AnnotationUtils.isIgnoreMethod;
import static com.atomic.annotations.AnnotationUtils.isScenario;
import static com.atomic.exception.ThrowException.throwNewException;
import static com.atomic.param.CallBack.paramAndResultCallBack;
import static com.atomic.param.Constants.HTTP_HEADER;
import static com.atomic.param.Constants.HTTP_HOST;
import static com.atomic.param.Constants.HTTP_METHOD;
import static com.atomic.param.Constants.HTTP_MODE;
import static com.atomic.param.Constants.LOGIN_URL;
import static com.atomic.param.HandleMethodName.getTestMethodName;
import static com.atomic.param.ParamPrint.resultPrint;
import static com.atomic.param.ParamUtils.isContentTypeNoNull;
import static com.atomic.param.ParamUtils.isHttpHeaderNoNull;
import static com.atomic.param.ParamUtils.isHttpHostNoNull;
import static com.atomic.param.ParamUtils.isLoginUrlNoNull;
import static com.atomic.param.ResultAssert.assertResultForRest;
import static com.atomic.param.ResultAssert.resultCallBack;
import static com.atomic.param.TestNGUtils.injectResultAndParameters;
import static com.atomic.param.assertcheck.AssertCheck.recMode;
import static com.atomic.param.assertcheck.AssertCheck.replayMode;
import static com.atomic.util.SaveResultUtils.saveTestRequestInCache;
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
 * @Data 2018/05/30 10:48
 */
// @Listeners({ScenarioRollBackListener.class, RollBackListener.class, ReportListener.class, SaveResultListener.class})
@Listeners({ScenarioRollBackListener.class, RollBackListener.class})
public abstract class BaseRestful extends AbstractInterfaceTest implements IHookable, ITestBase {

    protected final NewSqlTools newSqlTools = NewSqlTools.newInstance();

    @Override
    public void initDb() {

    }

    @AfterClass(alwaysRun = true)
    public void closeSqlTools() {
        newSqlTools.disconnect();
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
            throwNewException(e);
        }
    }

    private void startTest(IHookCallBack callBack, ITestResult testResult) throws Exception {

        Map<String, Object> context = TestNGUtils.getParamContext(testResult);

        //注入场景测试所需要的依赖方法的返回结果
        TestNGUtils.injectScenarioReturnResult(testResult, context);

        // 先执行beforeTestMethod
        beforeTest(context);

        // 把 excel 中的变量转换为真实值
        HandleExcelParam.getDataBeforeTest(new SqlTools(), context);

        // 检查Http接口测试入参必填字段
        ParamUtils.checkKeyWord(context);

        //记录方法调用开始时间
        SaveRunTime.startTestTime(testResult);

        // 执行接口调用
        Response response = startRequest(testResult, context);

        //记录方法调用结束时间
        SaveRunTime.endTestTime(testResult);

        // 缓存入参和返回值
        SaveResultUtils.saveTestResultInCache(response, testResult, context);

        execMethod(response, testResult, callBack, context);
    }

    @SuppressWarnings("unchecked")
    private Response startRequest(ITestResult testResult, Map<String, Object> context) {
        Headers headers;
        String httpHost;
        if (!isHttpHostNoNull(context)) {
            httpHost = CenterConfig.newInstance().getHttpHost();
        } else {
            httpHost = context.get(HTTP_HOST).toString();
        }
        RequestSpecification specification = given().baseUri(httpHost)
                // 编码设置
                .config(config().encoderConfig(encoderConfig().defaultContentCharset(defaultCharset())))
                // SSL 设置
                .config(config().sslConfig(sslConfig().allowAllHostnames()));

        //设置ContentType
        setContentType(specification, context);

        if (isLoginUrlNoNull(context)) {
            // 调用快捷登录
            headers = given().get(context.get(LOGIN_URL).toString()).getHeaders();
            specification = specification.headers(headers);
        } else if (isHttpHeaderNoNull(context) && context.get(HTTP_HEADER) != null) {

            Gson gson = new Gson();
            // 把Header json字符串反序列化为List<Header>
            String headerStr = context.get(HTTP_HEADER).toString();
            List<Map<String,String>> headerMapList = gson.fromJson(headerStr, new TypeToken<List<Map<String,String>>>() {
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
        return getResponse(testResult, specification, context);
    }

    private Response getResponse(ITestResult testResult, RequestSpecification specification, Map<String, Object> context) {
        Response response;
        String httpMode = context.get(HTTP_MODE).toString();
        String uri = context.get(HTTP_METHOD).toString();
        // Http接口入参
        Map<String, Object> parameters = ParamUtils.getParameters(context);

        if (isScenario(TestNGUtils.getTestMethod(testResult))) {
            // 保存测试场景接口入参对象
            saveTestRequestInCache(parameters, testResult, context);
        }
        if (Constants.HTTP_GET.equalsIgnoreCase(httpMode)) {
            if (parameters != null && parameters.size() > 0) {
                response = specification.params(parameters).when().get(uri);
            } else {
                response = specification.when().get(uri);
            }
        } else if (Constants.HTTP_POST.equalsIgnoreCase(httpMode) && isJsonContext(context)) {
            // POST Json请求
            if (parameters == null || parameters.size() == 0) {
                response = specification.when().post(uri);
            } else if (parameters.keySet().contains("request")) {
                response = specification.body(parameters.get("request").toString()).when().post(uri);
            } else {

                Map<String, Object> param = HandleExcelParam.assemblyParamMap2RequestMap(testResult, this, parameters);

                // 复制一份新的入参map集合，存入测试上下文中
                Map<String, Object> newParam = Maps.newHashMap(param);

                param.remove(Constants.CASE_INDEX);
                // 把param设置会测试上下文中
                TestNGUtils.setParamContext(testResult, newParam);

                response = specification.body(param).when().post(uri);
            }
        } else {
            // POST 表单提交
            if (parameters != null && parameters.size() > 0) {
                response = specification.params(parameters).when().post(uri);
            } else {
                response = specification.when().post(uri);
            }
        }
        return response;
    }

    /**
     * 判断 ContentType 是否为 application/json
     * @param param 入参 map 集合
     * @return boolean
     */
    private boolean isJsonContext(Map<String, Object> param) {
        return Objects.nonNull(param.get(Constants.CONTENT_TYPE)) && Constants.CONTENT_TYPE_JSON.equalsIgnoreCase(param.get(Constants.CONTENT_TYPE).toString());
    }

    private void execMethod(Response response, ITestResult testResult, IHookCallBack callBack, Map<String, Object> context) throws Exception {
        Map<String, Object> parameters = TestNGUtils.getParamContext(testResult);
        String result = response.body().asString();
        // 打印测试结果
        resultPrint(getTestMethodName(testResult), response, context, parameters);
        //回调函数，为testCase方法传入，入参和返回结果
        ITestResultCallback callback = paramAndResultCallBack();
        //返回result为String，则检测是否需要录制回放和自动断言
        if (AnnotationUtils.isAutoAssert(TestNGUtils.getTestMethod(testResult)) && ParamUtils.isAutoAssert(context)) {
            if (getCheckMode(TestNGUtils.getTestMethod(testResult)) == CheckMode.REC) {
                recMode(parameters, result, TestNGUtils.getHttpMethod(testResult));
                System.out.println("-----------------------执行智能化断言录制模式成功！-------------------------");
                resultCallBack(response, context, callback, parameters);
            } else if (getCheckMode(TestNGUtils.getTestMethod(testResult)) == CheckMode.REPLAY) {
                replayMode(parameters, result, TestNGUtils.getHttpMethod(testResult), context, callback);
                System.out.println("-----------------------执行智能化断言回放模式成功！-------------------------");
                resultCallBack(response, context, callback, parameters);
            } else {
                // 自动断言
                assertResultForRest(response, testResult, this, context, callback, parameters);
            }
        } else {
            // 自动断言
            assertResultForRest(response, testResult, this, context, callback, parameters);
        }
        testCallBack(callBack, testResult);
    }

    /**
     * 测试方法回调
     * @param callBack   回调函数
     * @param testResult 结果上下文
     */
    @SuppressWarnings("unchecked")
    private void testCallBack(IHookCallBack callBack, ITestResult testResult) {
        Map<String, Object> context = (Map<String, Object>) testResult.getParameters()[0];
        try {
            // 注入参数和结果，param 会去掉系统使用的一些数据
            injectResultAndParameters(context, testResult, this);
        } catch (Exception e) {
            Reporter.log("[BaseRestful#testCallBack()]:{} ---> 为TestCase方法注入入参和返回结果异常！");
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
