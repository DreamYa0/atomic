package com.atomic.tools.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.testng.Reporter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求过程实现
 * @author Yangminhan
 * @version 1.0
 *          <p>
 *          Created by Yangminhan on 2017/6/1.
 */
public abstract class HttpRequestProcess {

    public static final String ALLOW_CIRCULAR_REDIRECTS = "http.protocol.allow-circular-redirects";
    private static String userkey;
    private static String userid;
    private static String nickname;

    public static String getUserkey() {
        return userkey;
    }

    public static String getUserid() {
        return userid;
    }

    public static String getNickname() {
        return nickname;
    }

    /**
     * 快捷登录 - 获取快捷登录userkey,userid,nickname
     * @param url - "http://task.e1.zbjdev.com/api/login?uid=50"
     */
    public static void setZBJLogin(final String url, HttpClientContext context) {
        Args.notEmpty(url, "Fast Login URL");
        Args.notNull(context, "Http Client Context");
        HttpClient.getInstance().setClient();
        // 生成Get请求HttpUriRequest对象
        HttpRequestBase httpRequestBase = new HttpGet(url);
        // 发起Get请求，获取Http响应实体
        HttpClient.getInstance().getRequest(httpRequestBase, context, null);
        // 通过Http上下文获取Cookie成员userkey,userid,nickname
        try {
            HttpCltContext.getInstance().setCookieList(context);
            List<Cookie> cookies = HttpCltContext.getInstance().getCookieList();
            userkey = HttpCltContext.getInstance().getCookieValue(cookies, "userkey");
            userid = HttpCltContext.getInstance().getCookieValue(cookies, "userid");
            nickname = HttpCltContext.getInstance().getCookieValue(cookies, "nickname");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Http请求 - Get,Delete请求过程
     * 返回String
     * @param httpRequestBase
     * @param requestHeader
     * @param clientContext
     * @return
     * @throws Exception
     */
    public static String woEntityHttpRequest(final HttpRequestBase httpRequestBase, final List<Header> requestHeader, HttpClientContext clientContext) throws Exception {
        String result = null;
        // 发起HttpRequestBase请求，获取Http响应实体
        HttpEntity getEntity = getEntityWORequestEntity(httpRequestBase, requestHeader, clientContext);

        // 消费HttpRequestBase请求响应实体
        try {
            result = HttpClientEntity.getInstance().parseEntityContent(getEntity);
            HttpClientEntity.getInstance().closeEntity(getEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Http请求 - Get,Delete请求过程
     * 返回HttpEntity
     * @param httpRequestBase
     * @param requestHeader
     * @param clientContext
     * @return
     * @throws Exception
     */
    public static HttpEntity getEntityWORequestEntity(final HttpRequestBase httpRequestBase, final List<Header> requestHeader, HttpClientContext clientContext) {
        Args.notNull(httpRequestBase, "Http request Type:get/delete");
        HttpEntity getEntity = null;

        // 设置Http请求Header
        HttpClientHeader.getInstance().setRequestHeader(httpRequestBase, requestHeader);
        HttpClient.getInstance().setClient();
        // 发起HttpRequestBase请求，获取Http响应实体
        getEntity = HttpClient.getInstance().getRequest(httpRequestBase, clientContext, requestHeader);

        return getEntity;
    }

    /**
     * Http请求 - Get,Delete生成对应HttpRequestBase对象
     * @param httpMode
     * @param url
     * @param parameters
     * @return
     */
    public static HttpRequestBase getHttpRequestBase(String httpMode, String url, Map<String, Object> parameters) {
        HttpRequestBase httpRequestBase = null;
        if (httpMode.toLowerCase().equals("get")) {
            Args.notNull(parameters, "Http Get request in parameters");
            // new Get请求
            httpRequestBase = new HttpGet(assembleGetURL(url, parameters));
            // 设置重定向
            /*RequestConfig requestConfig = RequestConfig.custom()
                    .setRedirectsEnabled(true)
                    .setRelativeRedirectsAllowed(true)
                    .setCircularRedirectsAllowed(true)
                    .setMaxRedirects(1000).build();
            httpRequestBase.setConfig(requestConfig);*/
            //httpRequestBase.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        }
        if (httpMode.toLowerCase().equals("delete")) {
            // new Delete请求
            httpRequestBase = new HttpDelete(url);
        }
        return httpRequestBase;
    }

    /**
     * 组装Http Get请求Url
     * @param url
     * @param inParam
     * @return
     */
    private static String assembleGetURL(String url, Map<String, Object> inParam) {
        StringBuffer sb = new StringBuffer();
        if (inParam.isEmpty()) {
            sb.append(url);
        } else {
            sb.append(url).append("?");
            Iterator<Map.Entry<String, Object>> iterMap = inParam.entrySet().iterator();
            while (iterMap.hasNext()) {
                Map.Entry<String, Object> entry = iterMap.next();
                try {
                    sb.append(entry.getKey())
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue().toString(), "utf-8"))
                            .append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return new String(sb);
    }

    /**
     * Http请求 - Post,Put请求过程
     * 返回String
     * @param httpEntityRequest
     * @param httpEntity
     * @param requestHeader
     * @param clientContext
     * @return
     * @throws Exception
     */
    public static String wEntityHttpRequest(final HttpEntityEnclosingRequestBase httpEntityRequest, final HttpEntity httpEntity, final List<Header> requestHeader, HttpClientContext clientContext) throws Exception {
        String result = null;

        HttpEntity responseEntity = getEntityWRequestEntity(httpEntityRequest, httpEntity, requestHeader, clientContext);
        // 消费HttpEntityEnclosingRequestBase请求响应实体
        try {
            result = HttpClientEntity.getInstance().parseEntityContent(responseEntity);
            HttpClientEntity.getInstance().closeEntity(responseEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Http请求 - Post,Put请求过程
     * 返回HttpEntity
     * @param httpEntityRequest
     * @param httpEntity
     * @param requestHeader
     * @param clientContext
     * @return
     * @throws Exception
     */
    public static HttpEntity getEntityWRequestEntity(final HttpEntityEnclosingRequestBase httpEntityRequest, final HttpEntity httpEntity, final List<Header> requestHeader, HttpClientContext clientContext) {
        Args.notNull(httpEntityRequest, "HttpEntityEnclosingRequestBase ");
        Args.notNull(httpEntity, "HttpEntity");

        HttpEntity responseEntity = null;
        HttpClient.getInstance().setClient();
        // 发起HttpEntityEnclosingRequestBase请求，获取Http响应实体
        responseEntity = HttpClient.getInstance().postRequest(httpEntityRequest, httpEntity, clientContext, requestHeader);

        return responseEntity;
    }

    /**
     * Http请求 - Post,Put生成对应HttpEntityEnclosingRequestBase对象
     * @param httpMode
     * @param url
     * @return
     */
    public static HttpEntityEnclosingRequestBase setHttpEntityRequestBase(String httpMode, String url) {
        HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = null;
        if (httpMode.toLowerCase().equals("post")) {
            // new Post请求
            httpEntityEnclosingRequestBase = new HttpPost(url);
        }
        if (httpMode.toLowerCase().equals("put")) {
            // new Put请求
            httpEntityEnclosingRequestBase = new HttpPut(url);
        }
        return httpEntityEnclosingRequestBase;
    }

    /**
     * 发送get请求
     * @param url 路径
     * @return
     */
    public static String httpGet(String url) {
        //get请求返回结果
        String jsonResult = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            org.apache.http.HttpResponse response = client.execute(request);
            //请求发送成功，并得到响应
            if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                //读取服务器返回过来的json字符串数据
                //把json字符串转换成json对象
                //				jsonResult = JSONObject.fromObject(strResult);
                jsonResult = EntityUtils.toString(response.getEntity());
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                Reporter.log("get请求提交失败:" + url, true);
            }
        } catch (Exception e) {
            Reporter.log("get请求提交失败:" + url, true);
        }
        return jsonResult;
    }
}
