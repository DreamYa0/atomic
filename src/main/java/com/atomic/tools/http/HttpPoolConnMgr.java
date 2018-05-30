package com.atomic.tools.http;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.stereotype.Component;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * HttpClient - 连接池
 * @author Yangminhan
 * @version 1.0
 * @Data 2017/9/25
 */
@Component
public class HttpPoolConnMgr {
    private static Integer MAX_CONN_NUM = 100;
    private static Integer MAX_PER_ROUTE = 20;
    private Registry<ConnectionSocketFactory> registry;

    public HttpPoolConnMgr() {
    }

    public HttpPoolConnMgr initPoolConnection() {
        // scheme = http,调用明文连接套节工厂建立连接
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);

        // scheme = https,调用SSL连接套接字工厂建立连接
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        try {
            sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
            registryBuilder.register("https", socketFactory);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }

        this.registry = registryBuilder.build();
        return this;
    }

    /**
     * 设置连接池 - by默认路由
     * @return
     */
    public PoolingHttpClientConnectionManager getDefaultPoolConnMgr() {
        PoolingHttpClientConnectionManager poolConnMgr = new PoolingHttpClientConnectionManager(this.registry);
        poolConnMgr.setMaxTotal(MAX_CONN_NUM);
        poolConnMgr.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        return poolConnMgr;
    }

    /**
     * 设置连接池 - by自定义路由
     * @return
     */
    public PoolingHttpClientConnectionManager getCustomPoolConnMgr(HttpRoute route) {
        PoolingHttpClientConnectionManager poolConnMgr = new PoolingHttpClientConnectionManager(this.registry);
        poolConnMgr.setMaxTotal(MAX_CONN_NUM);
        poolConnMgr.setMaxPerRoute(route, MAX_PER_ROUTE);

        return poolConnMgr;
    }
}
