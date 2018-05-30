package com.atomic.tools.http;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.Args;

import java.util.List;

/**
 * HttpClient上下文工具类
 * @author Yangminhan
 * @version 1.0
 *          <p>
 *          Created by Yangminhan on 2017/6/2.
 */
final class HttpClientHeader {

    private static final HttpClientHeader INSTANCE = new HttpClientHeader();

    private HttpClientHeader() {
    }

    public static HttpClientHeader getInstance() {
        return INSTANCE;
    }

    /**
     * 获取HTTP Response指定Header
     * @param headers
     * @param headerName
     * @return
     */
    Header getResponseHeader(Header[] headers, String headerName) {
        Args.notNull(headers, "HTTP Response Handle");
        Args.notEmpty(headerName, "HTTP Header Name");
        Header assignHeader = null;
        for (Header header : headers) {
            if (header.getName().equals(headerName)) {
                assignHeader = header;
            }
        }
        return assignHeader;
    }

    /**
     * 设置Http Request Header
     * @param httpUriRequest
     * @param header
     */
    private void setRequestHeader(HttpUriRequest httpUriRequest, Header header) {
        Args.notNull(httpUriRequest, "HTTP Get Request");
        Args.notNull(header, "HTTP Request Header");
        httpUriRequest.setHeader(header);
    }

    /**
     * 设置Http Request Header
     * @param httpUriRequest
     * @param headerList
     */
    void setRequestHeader(HttpUriRequest httpUriRequest, List<Header> headerList) {
        Args.notNull(httpUriRequest, "HTTP Get Request");
        // Header List不为null,且不为空则设置Http请求Header
        if (null != headerList && !headerList.isEmpty()) {
            for (Header header : headerList) {
                setRequestHeader(httpUriRequest, header);
            }
        }
    }
}
