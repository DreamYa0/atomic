package com.atomic.tools.http;

import com.atomic.exception.HttpInterfaceException;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.testng.Reporter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * HttpClient实体工具类
 * @author Yangminhan
 * @version 1.0
 *          <p>
 *          Created by Yangminhan on 2017/6/1.
 */
public final class HttpClientEntity {

    private static final HttpClientEntity INSTANCE = new HttpClientEntity();

    private HttpClientEntity() {
    }

    public static HttpClientEntity getInstance() {
        return INSTANCE;
    }

    public HttpEntity getHttpClientEntity(Map<String, Object> parameters, String contentType) throws Exception {
        HttpEntity httpEntity;
        if (contentType.toLowerCase().contains("x-www-form-urlencoded")) {
            httpEntity = getUrlEncodedFormEntity(HttpClientComponent.getInstance().getNameValuePairList(parameters), contentType);
        } else if (contentType.toLowerCase().contains("json")) {
            httpEntity = getStringEntity(HttpClientComponent.getInstance().getJSONString(parameters), contentType);
        } else if (contentType.toLowerCase().contains("octet-stream")) {
            httpEntity = getByteArrayEntity(HttpClientComponent.getInstance().getByteArray(parameters), contentType);
        } else {
            Reporter.log("[HttpClientEntity#getHttpClientEntity()]:{Http请求实体媒体类型添加. Content Type:"
                    + contentType + "}");
            throw new HttpInterfaceException("Http请求实体媒体类型添加. Content Type:"
                    + contentType);
        }
        return httpEntity;
    }

    /**
     * 设置ByteArrayEntity - 默认："application/octet-stream,utf-8"
     * @param bytes
     * @param contentType
     */
    private ByteArrayEntity getByteArrayEntity(byte[] bytes, String contentType) {
        Args.notNull(contentType, "Http Post Entity Content Type");
        ByteArrayEntity byteArrayEntity = null;
        // 设置ContentType, default = "application/octet-stream,utf-8"
        ContentType cType = null;
        if (!contentType.isEmpty()) {
            cType = ContentType.create(contentType, Consts.UTF_8);
        } else {
            cType = ContentType.create("application/octet-stream", Consts.UTF_8);
        }
        byteArrayEntity = new ByteArrayEntity(bytes, cType);
        byteArrayEntity.setChunked(true);
        return byteArrayEntity;
    }

    /**
     * 设置StringEntity - 默认："application/json,utf-8"
     * @param strEntity
     * @param contentType
     */
    private StringEntity getStringEntity(String strEntity, String contentType) {
        Args.notNull(contentType, "Http Post Entity Content Type");
        StringEntity stringEntity = null;
        // 设置ContentType, default = "application/json,utf-8"
        ContentType cType = null;
        if (!contentType.isEmpty()) {
            cType = ContentType.create(contentType, Consts.UTF_8);
        } else {
            cType = ContentType.APPLICATION_JSON;
        }
        stringEntity = new StringEntity(strEntity, cType);
        stringEntity.setChunked(true);
        return stringEntity;
    }

    /**
     * 设置UrlEncodedFormEntity - "application/x-www-form-urlencoded,utf-8"
     * @param form
     * @param contentType
     */
    private UrlEncodedFormEntity getUrlEncodedFormEntity(final List<NameValuePair> form, final String contentType) {
        Args.notEmpty(form, "Http Post Pair Form");
        Args.notNull(contentType, "Http Post Entity Content Type");
        // 设置ContentType, default = "application/x-www-form-urlencoded,utf-8"
        Charset charset = null;
        if (!contentType.isEmpty()) {
            charset = Consts.UTF_8;
        }
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(form, charset);
        urlEncodedFormEntity.setChunked(true);
        return urlEncodedFormEntity;
    }

    /**
     * Http请求 - 解析Response Entity
     * ContentType - application/json,text/html,application/octet-stream
     * @param entity
     * @return
     * @notice - response entity = text/html,暂时不处理
     * @notice - response entity = application/octet-stream,需反序列化处理
     */
    protected String parseEntityContent(HttpEntity entity) throws Exception {
        Args.notNull(entity, "Http Post Entity");
        String result;
        String contentType = entity.getContentType().getValue();
        if (contentType.toLowerCase().contains("application/json") ||
                contentType.toLowerCase().contains("text/html") ||
                contentType.toLowerCase().contains("text/plain")) {
            if (entity.getContentLength() < 0 ||
                    entity.getContentLength() > 4096) {
                result = HttpClientComponent.getInstance().inStreamToString(entity.getContent());
            } else {
                result = EntityUtils.toString(entity, Consts.UTF_8);
            }
            return HttpClientComponent.getInstance().revert2GBString(result);
        } else if (contentType.toLowerCase().contains("application/octet-stream")) {
            byte[] bResults;
            if (entity.getContentLength() < 0 ||
                    entity.getContentLength() > 4096) {
                bResults = HttpClientComponent.getInstance().inStreamToByteArray(entity.getContent(), entity.getContentLength());
            } else {
                bResults = EntityUtils.toByteArray(entity);
            }
            return new String(bResults);
        } else {
            Reporter.log("[HttpClientEntity#parseEntityContent()]:{Http接口响应实体媒体类型待添加. ContentType:"
                    + entity.getContentType().getValue() + "}");
            throw new HttpInterfaceException("Http接口响应实体媒体类型待添加. ContentType:"
                    + entity.getContentType().getValue());
        }
    }

    /**
     * Http请求 - 设置Request Entity
     * @param httpEntityEnclosingRequest
     * @param httpEntity
     */
    protected void setHttpEntity(HttpEntityEnclosingRequest httpEntityEnclosingRequest, HttpEntity httpEntity) {
        Args.notNull(httpEntityEnclosingRequest, "HttpEntityEnclosingRequest implement class");
        if (null != httpEntity) {
            httpEntityEnclosingRequest.setEntity(httpEntity);
        }
    }

    /**
     * 消费HttpEntity实体 - Entity使用完后建议消费
     * @param entity
     */
    protected void closeEntity(HttpEntity entity) {
        Args.notNull(entity, "Http Post Entity");
        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
