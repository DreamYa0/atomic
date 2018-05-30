package com.atomic.tools.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HttpClient
 * @author dreamyao
 * @version 1.0
 * @Data 2018/05/30 10:48
 */
@Component
public class HttpClientMgr {
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;

    @Autowired
    private HttpProxyMgr httpProxy;

    @Autowired
    private HttpPoolConnMgr httpPoolConnection;

    public void getDefaultHttpClient() {
        this.httpClient = HttpClients.custom()
                .setConnectionManager(httpPoolConnection.getDefaultPoolConnMgr())
                .setRoutePlanner(httpProxy.initProxy().getDefaultRoutePlanner())
                .build();
    }

    public void getCustomHttpClient() {
        this.httpClient = HttpClients.custom()
                .setRoutePlanner(httpProxy.getCustomRoutePlanner())
                .setConnectionManager(httpPoolConnection.initPoolConnection().getDefaultPoolConnMgr())
                .build();
    }

    /*public void closeConnectionHttpClient(HttpHost target, org.apache.http.Request request, ResponseHandler responseHandler, HttpContext context) {
        try {
            this.httpResponse = this.httpClient.execute(target, request, responseHandler, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
