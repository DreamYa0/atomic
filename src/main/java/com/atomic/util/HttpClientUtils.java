package com.atomic.util;

import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dreamyao
 */
public class HttpClientUtils {

    private CloseableHttpClient httpClient;
    private RequestConfig requestConfig;
    private Integer maxTimeOut = 7000;
    private List<String> results = Lists.newArrayList();

    private HttpClientUtils() {
        initConfig();
        httpClient = HttpClients.createDefault();
    }

    private HttpClientUtils(CloseableHttpClient httpClient) {
        initConfig();
        this.httpClient = httpClient;
    }

    private HttpClientUtils(String proxyIp, Integer proxyPort) {
        this(proxyIp, proxyPort, null);
    }

    private HttpClientUtils(String proxyIp, Integer proxyPort, String scheme) {
        if (proxyIp == null || proxyPort == null) {
            return;
        }
        HttpHost host = new HttpHost(proxyIp, proxyPort, scheme);
        initConfig(host);
        httpClient = HttpClients.createDefault();
    }

    public static HttpClientUtils createZBJHttpClient() {
        return new HttpClientUtils();
    }

    public static HttpClientUtils createZBJHttpClient(CloseableHttpClient httpClient) {
        return new HttpClientUtils(httpClient);
    }

    /**
     * 初始化一个设置了代理的ZBJHttpClient对象
     * @param proxyIp   代理IP地址
     * @param proxyPort 代理端口号
     */
    public static HttpClientUtils createZBJHttpClient(String proxyIp, Integer proxyPort) {
        return new HttpClientUtils(proxyIp, proxyPort);
    }

    /**
     * 初始化一个设置了代理的ZBJHttpClient对象
     * @param proxyIp   代理IP地址
     * @param proxyPort 代理端口号
     * @param scheme    http,https
     */
    public static HttpClientUtils createZBJHttpClient(String proxyIp, Integer proxyPort, String scheme) {
        return new HttpClientUtils(proxyIp, proxyPort, scheme);
    }

    /**
     * 获取一个JSON的header
     * @return 请求头信息
     */
    public static Header getJSONHeader() {
        return new BasicHeader("Content-Type", "application/json");
    }

    private static String getParamsStr(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        params.keySet().forEach(key -> {
            String value = params.get(key).toString();
            sb.append(key).append("=").append(value);
            sb.append("&");
        });
        return sb.toString();
    }

    /**
     * 创建一个信任所有域名且不校验域名的规格的httpClient,用于Https请求
     */
    public final HttpClientUtils createSSLClient() {
        try {
            //信任所有
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            return this;
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            System.out.println("------------------创建用于HTTPS请求的HttpClient失败，现创建默认的HttpClient---------------");
            e.printStackTrace();
            httpClient = HttpClients.createDefault();
            return this;
        }
    }

    /**
     * 发送 GET 请求（HTTP,HTTPS），无参
     * @param url     请求Url地址
     * @param headers 请求头信息
     * @return Json字符串
     */
    @SafeVarargs
    public final HttpClientUtils doGet(String url, List<Header>... headers) {
        try {
            HttpGet httpGet = new HttpGet(url);
            initHeaders(httpGet, headers);
            results.add(getResponse(httpClient, httpGet, url));
        } catch (IOException e) {
            System.out.println("--------------------" + url + "--------------------");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 发送 GET 请求（HTTP,HTTPS），K-V形式
     * @param url    请求Url地址
     * @param params 请求参数
     * @return Json字符串
     */
    @SafeVarargs
    public final HttpClientUtils doGet(String url, Map<String, Object> params, List<Header>... headers) {
        String paramString = getParamsStr(params);
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        if (paramString.length() > 0) {
            sb.append("?");
            sb.append(paramString);
        }
        System.out.println("-----------------------url " + sb.toString() + "-----------------------");
        return doGet(sb.toString(), headers);
    }

    /**
     * 发送 POST 请求（HTTP,HTTPS），无参
     * @param url     请求Url地址
     * @param headers 请求头信息
     * @return Json字符串
     */
    public final HttpClientUtils doPost(String url, List<Header>... headers) {
        try {
            HttpPost httpPost = new HttpPost(url);
            initHeaders(httpPost, headers);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            results.add(getResponse(httpClient, httpPost, url));
        } catch (Exception e) {
            System.out.println("-----------------------" + url + "-----------------------");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 发送 POST 请求（HTTP,HTTPS），K-V形式
     * @param url     请求Url地址
     * @param params  表单参数
     * @param headers 请求头信息
     * @return Json字符串
     */
    @SafeVarargs
    public final HttpClientUtils doPost(String url, Map<String, Object> params, List<Header>... headers) {
        try {
            HttpPost httpPost = new HttpPost(url);
            initHeaders(httpPost, headers);
            List<NameValuePair> nvps = Lists.newArrayList();
            params.keySet().forEach(key -> nvps.add(new BasicNameValuePair(key, params.get(key).toString())));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            results.add(getResponse(httpClient, httpPost, url));
        } catch (Exception e) {
            System.out.println("----------------------- 请求失败：" + url + "-----------------------");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 发送 POST 请求（HTTP,HTTPS），JSON形式
     * @param url       请求地址
     * @param jsonParam 请求入参
     * @return Json字符串
     */
    @SafeVarargs
    public final HttpClientUtils doPost(String url, String jsonParam, List<Header>... headers) {
        HttpPost httpPost = new HttpPost(url);
        initHeaders(httpPost, headers);
        try {
            StringEntity stringEntity = new StringEntity(jsonParam, ContentType.APPLICATION_JSON);
            stringEntity.setContentEncoding("UTF-8");
            httpPost.setEntity(stringEntity);
            results.add(getResponse(httpClient, httpPost, url));
        } catch (IOException e) {
            System.out.println("----------------------- 请求失败：" + url + "-----------------------");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 获取 GET 请求的响应信息
     * @param url     请求 URL 地址
     * @param headers 请求头信息
     * @return 响应信息
     */
    @SafeVarargs
    public final HttpResponse doGetResponse(String url, List<Header>... headers) {
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            initHeaders(httpGet, headers);
            return httpClient.execute(httpGet);
        } catch (IOException e) {
            System.out.println(url);
            e.printStackTrace();
        }
        return null;
    }

    public List<String> toResult() {
        return results;
    }

    /**
     * 设置请求配置
     * @param requestConfig 请求配置
     */
    public HttpClientUtils setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    /**
     * 设置超时时间
     * @param maxTimeOut 超时时间
     */
    public HttpClientUtils setMaxTimeOut(Integer maxTimeOut) {
        this.maxTimeOut = maxTimeOut;
        return this;
    }

    private void initConfig(HttpHost... hosts) {
        // 设置连接池
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(maxTimeOut);
        // 设置读取超时
        configBuilder.setSocketTimeout(maxTimeOut);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(maxTimeOut);
        // 在提交请求之前 测试连接是否可用
        configBuilder.setStaleConnectionCheckEnabled(true);
        if (hosts.length == 1) {
            configBuilder.setProxy(hosts[0]);
        }
        requestConfig = configBuilder.build();
    }

    private String getResponse(CloseableHttpClient httpclient, HttpRequestBase httpEntity, String url) throws IOException {
        if (requestConfig != null) {
            httpEntity.setConfig(requestConfig);
        }
        HttpResponse response = httpclient.execute(httpEntity);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        } else {
            System.out.println(response.getStatusLine().getStatusCode() + " 请求提交失败:" + url);
            return null;
        }
    }

    @SafeVarargs
    private final void initHeaders(HttpRequestBase httpEntity, List<Header>... headers) {
        if (headers == null || headers.length == 0) {
            return;
        }
        if (headers[0] != null && headers[0].size() != 0) {
            (headers[0]).forEach(httpEntity::setHeader);
        }
    }
}
