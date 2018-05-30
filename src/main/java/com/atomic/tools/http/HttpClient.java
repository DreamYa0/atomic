package com.atomic.tools.http;

import com.atomic.config.GlobalConfig;
import com.atomic.enums.TestMode;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.testng.Reporter;

import java.io.IOException;
import java.util.List;

/**
 * HttpClient请求工具类
 * @author Yangminhan
 * @version 1.0
 *          <p>
 *          Created by Yangminhan on 2017/5/27.
 */
public class HttpClient {

    private static final HttpClient INSTANCE = new HttpClient();
    private CloseableHttpClient client;
    private CloseableHttpResponse response;
    private HttpEntity httpEntity;
    private Header[] headers;

    private HttpClient() {
    }

    public static HttpClient getInstance() {
        return INSTANCE;
    }

    /**
     * 提供代理手动设置功能
     * @param proxyHost 代理IP地址
     * @param proxyPort 代理端口号
     */
    public void setProxy(String proxyHost, Integer proxyPort) {
        setProxy(proxyHost, proxyPort, null);
    }

    /**
     * 提供代理手动设置功能
     * @param proxyHost 代理IP地址
     * @param proxyPort 代理端口号
     * @param scheme    http,https
     */
    public void setProxy(String proxyHost, Integer proxyPort, String scheme) {
        ProxyInfo proxyInfo = getProxyInfo();
        proxyInfo.setProxyHost(proxyHost);
        proxyInfo.setProxyPort(proxyPort);
        proxyInfo.setSchema(scheme);
    }

    /**
     * Http请求 - Get,Delete实例化
     * @param httpRequestBase
     * @param clientContext   - 不需要联系上下文时，设置为null
     * @return
     */
    protected HttpEntity getRequest(HttpRequestBase httpRequestBase, HttpClientContext clientContext, List<Header> requestHeader) {
        Args.notNull(httpRequestBase, "HttpRequestBase handle");
        if (TestMode.TESTING.getName().equals(getProfile())) {
            RequestConfig config = RequestConfig.custom().setProxy(getProxy()).build();
            httpRequestBase.setConfig(config);
        }
        try {
            // 设置HttpRequestBase请求Header
            if (null != requestHeader && !requestHeader.isEmpty()) {
                HttpClientHeader.getInstance().setRequestHeader(httpRequestBase, requestHeader);
            }
            // 执行HttpRequestBase请求
            if (clientContext != null) {
                this.response = this.client.execute(httpRequestBase, clientContext);
            } else {
                this.response = this.client.execute(httpRequestBase);
            }
            // 获取HttpRequestBase请求响应Header
            headers = null;
            headers = response.getAllHeaders();
            // 获取HttpRequestBase请求响应Entity
            setHttpEntity();
            getResponseEntity();
            return httpEntity;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放http连接
            closeHttpConnection();
        }
        return null;
    }

    /**
     * Http请求 - Post,Put实例化
     * @param httpEntityEnclosingRequestBase HttpPost、HttpGet基类
     * @param postEntity                     Entity = null，则提交空实体Post请求
     * @param clientContext                  不需要联系上下文时，设置为null
     * @return Http实体
     */
    protected HttpEntity postRequest(HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase, HttpEntity postEntity, HttpClientContext clientContext, List<Header> requestHeader) {
        Args.notNull(httpEntityEnclosingRequestBase, "httpEntityEnclosingRequestBase handle");
        if (TestMode.TESTING.getName().equals(getProfile())) {
            RequestConfig config = RequestConfig.custom().setProxy(getProxy()).build();
            httpEntityEnclosingRequestBase.setConfig(config);
        }
        try {
            // 设置HttpEntityEnclosingRequestBase请求Header
            HttpClientHeader.getInstance().setRequestHeader(httpEntityEnclosingRequestBase, requestHeader);
            // 设置HttpEntityEnclosingRequestBase请求Entity
            HttpClientEntity.getInstance().setHttpEntity(httpEntityEnclosingRequestBase, postEntity);
            // 执行HttpEntityEnclosingRequestBase请求
            if (clientContext != null) {
                this.response = this.client.execute(httpEntityEnclosingRequestBase, clientContext);
            } else {
                this.response = this.client.execute(httpEntityEnclosingRequestBase);
            }
            // 获取HttpEntityEnclosingRequestBase请求响应Header
            this.headers = null;
            this.headers = this.response.getAllHeaders();
            // 获取HttpEntityEnclosingRequestBase请求响应Entity
            setHttpEntity();
            getResponseEntity();
            return this.httpEntity;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 释放http连接
            closeHttpConnection();
        }
        return null;
    }

    private void getResponseEntity() {
        try {
            if (HttpStatus.SC_OK == this.response.getStatusLine().getStatusCode()) {
                HttpEntity entity = this.response.getEntity();
                if (null != entity.getContentEncoding() &&
                        entity.getClass().getSimpleName().equalsIgnoreCase("DecompressingEntity")) {
                    this.httpEntity = entity;
                } else {
                    // 获取服务器响应实体
                    this.httpEntity = new BufferedHttpEntity(entity);
                }
            } else {
                Reporter.log("[HttpClient#getResponseEntity()]:{HTTP Status Code Error, HTTP Status code:"
                        + this.response.getStatusLine().getStatusCode() + "}");
                /*throw new HttpInterfaceException("HTTP Status Code Error, HTTP Status code:"
                        + this.response.getStatusLine().getStatusCode());*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放http连接
            closeHttpConnection();
        }
    }

    private String getProfile() {
        GlobalConfig.load();
        return GlobalConfig.getProfile();
    }

    /**
     * 获取代理设置
     * @return
     */
    private HttpHost getProxy() {
        ProxyInfo proxyInfo = getProxyInfo();
        return new HttpHost(proxyInfo.getProxyHost(), proxyInfo.getProxyPort(), proxyInfo.getScheme());
    }

    private ProxyInfo getProxyInfo() {
        HttpClient httpClientUtils = new HttpClient();
        return httpClientUtils.new ProxyInfo();
    }

    public CloseableHttpClient getClient() {
        return this.client;
    }

    public void setClient() {
        this.client = HttpClientBuilder.create().build();
    }

    /**
     * 获取Http Response Headers
     * @return
     */
    public Header[] getHeaders() {
        return this.headers;
    }

    public HttpEntity getHttpEntity() {
        return this.httpEntity;
    }

    private void setHttpEntity() {
        this.httpEntity = null;
    }

    /**
     * 释放资源
     */
    private void closeHttpConnection() {
        try {
            // 关闭响应
            response.close();
            // 释放http连接
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ProxyInfo {

        private String proxyHost = "172.26.21.45";
        private Integer proxyPort = 3128;
        private String scheme = "http";

        public String getProxyHost() {
            return proxyHost;
        }

        public void setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
        }

        public Integer getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
        }

        public String getScheme() {
            return scheme;
        }

        public void setSchema(String scheme) {
            this.scheme = scheme;
        }
    }

}
