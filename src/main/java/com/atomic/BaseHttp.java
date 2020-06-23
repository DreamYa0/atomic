package com.atomic;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import com.atomic.exception.InjectResultException;
import com.atomic.exception.ParameterException;
import com.atomic.tools.report.ReportListener;
import com.atomic.tools.rollback.RollBackListener;
import com.atomic.tools.rollback.ScenarioRollBackListener;
import com.atomic.param.Constants;
import com.atomic.param.ITestResultCallback;
import com.atomic.param.ParamUtils;
import com.atomic.util.TestNGUtils;
import com.atomic.tools.http.GetHandler;
import com.atomic.tools.http.IHandler;
import com.atomic.tools.http.PostHandler;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Listeners;

import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

import static com.atomic.annotations.AnnotationUtils.isIgnoreMethod;
import static com.atomic.exception.ThrowException.throwNewException;
import static com.atomic.tools.report.SaveRunTime.endTestTime;
import static com.atomic.tools.report.SaveRunTime.startTestTime;
import static com.atomic.param.CallBack.paramAndResultCallBack;
import static com.atomic.param.Constants.HTTP_HEADER;
import static com.atomic.param.Constants.HTTP_HOST;
import static com.atomic.param.Constants.HTTP_PROXY;
import static com.atomic.tools.report.HandleMethodName.getTestMethodName;
import static com.atomic.tools.report.ParamPrint.resultPrint;
import static com.atomic.param.ParamUtils.isHttpHeaderNoNull;
import static com.atomic.param.ParamUtils.isHttpHostNoNull;
import static com.atomic.param.ParamUtils.isLoginUrlNoNull;
import static com.atomic.tools.assertcheck.ResultAssert.assertResult;
import static com.atomic.util.TestNGUtils.injectResultAndParameters;
import static com.atomic.tools.report.SaveResultCache.saveTestResultInCache;


/**
 * @author dreamyao
 * @version 1.0.0
 * @description 新的 HTTP 请求基类
 * @Data 2018/05/30 10:48
 */
@Listeners({ScenarioRollBackListener.class, RollBackListener.class, ReportListener.class})
public abstract class BaseHttp extends AbstractRestTest implements IHookable, ITestBase {

    /**
     * Http接口入参字段检查
     * @param context excel入参
     */
    private static void checkHttpKeyWord(Map<String, Object> context) {
        if (!ParamUtils.isHttpModeNoNull(context)) {
            Reporter.log("Http Mode not be empty");
            throw new ParameterException("Http Mode not be empty.");
        }
        if (!ParamUtils.isHttpMethodNoNull(context)) {
            Reporter.log("Http Method not be null");
            throw new ParameterException("Http Method not be null.");
        }
    }

    @Override
    public void initDb() {

    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        // 有Ignore注解，就直接转测试代码
        if (isIgnoreMethod(TestNGUtils.getTestMethod(testResult))) {
            callBack.runTestMethod(testResult);
            return;
        }
        try {
            prepareTest(callBack, testResult);
        } catch (Exception e) {
            throwNewException(e);
        }
    }

    private void prepareTest(IHookCallBack callBack, ITestResult testResult) throws Exception {
        Map<String, Object> context = TestNGUtils.getParamContext(testResult);
        //注入场景测试所需要的依赖方法的返回结果
        TestNGUtils.injectScenarioReturnResult(testResult, context);
        // 先执行beforeTestMethod
        beforeTest(context);
        // 检查Http接口测试入参必填字段
        checkHttpKeyWord(context);
        prepareRequest(context, callBack, testResult);
    }

    private void prepareRequest(Map<String, Object> context,
                                IHookCallBack callBack,
                                ITestResult testResult) throws Exception {

        String httpHost;
        if (!isHttpHostNoNull(context)) {
            httpHost = CenterConfig.newInstance().getHttpHost();
        } else {
            httpHost = context.get(HTTP_HOST).toString();
        }
        HttpRequest request = new HttpRequest(httpHost);
        if (Constants.PROFILE_TESTING.equals(GlobalConfig.getProfile())) {
            // 预发布环境设置代理
            String httpProxy = CenterConfig.newInstance().readPropertyConfig(
                    GlobalConfig.getProfile()).get(HTTP_PROXY);
            String host = httpProxy.split(":")[0];
            int port = Integer.parseInt(httpProxy.split(":")[1]);
            InetSocketAddress address = new InetSocketAddress(host, port);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
            request.setProxy(proxy);
        }
        if (isLoginUrlNoNull(context)) {
            String loginUrl = context.get(Constants.LOGIN_URL).toString();
            HttpRequest httpRequest = new HttpRequest(loginUrl);
            HttpResponse httpResponse = httpRequest.execute();
            List<HttpCookie> cookies = httpResponse.getCookies();
            request.cookie((HttpCookie[]) cookies.toArray());
        }
        if (isHttpHeaderNoNull(context)) {
            String headerString = context.get(HTTP_HEADER).toString();
            Map<String, String> headerMap = JSON.parseObject(headerString,
                    new TypeReference<Map<String, String>>() {
            });
            headerMap.forEach(request::header);
        }
        execute(context, callBack, testResult, request);
    }

    private void execute(Map<String, Object> context,
                         IHookCallBack callBack,
                         ITestResult testResult,
                         HttpRequest request) throws Exception {

        IHandler getHandler = new GetHandler();
        IHandler postHandler = new PostHandler();
        getHandler.setHandler(postHandler);

        //记录方法调用开始时间
        startTestTime(testResult);

        HttpResponse response = getHandler.handle(context, request);

        //记录方法调用结束时间
        endTestTime(testResult);

        saveTestResultInCache(response, testResult, context);

        handleResponse(response, testResult, callBack, context);
    }

    private void handleResponse(HttpResponse response,
                                ITestResult testResult,
                                IHookCallBack callBack,
                                Map<String, Object> context) throws Exception {

        String result = response.body();

        // 打印测试结果
        resultPrint(getTestMethodName(testResult), result, context);

        //回调函数，为testCase方法传入，入参和返回结果
        ITestResultCallback callback = paramAndResultCallBack();
        // 自动断言
        assertResult(result, context, callback);
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
            Reporter.log("为TestCase方法注入入参和返回结果时出现异常！");
            throw new InjectResultException(e);
        }
        //回调测试方法
        callBack.runTestMethod(testResult);
    }
}
