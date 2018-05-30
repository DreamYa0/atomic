package com.atomic.tools.http;

import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * HttpClient - 代理
 * @author Yangminhan
 * @version 1.0
 * @Data 2017/9/25
 */
@Component
public class HttpProxyMgr {
    private Map<String, String> maps;

    public HttpProxyMgr() {
    }

    public HttpProxyMgr initProxy() {
        GlobalConfig.load();//加载环境配置文件
        String profile = GlobalConfig.getProfile();
        this.maps = CenterConfig.newInstance().readPropertyConfig(profile);
        return this;
    }

    /**
     * @return
     * @throws URISyntaxException
     * @Funtion 设置简单代理 - by配置中心
     */
    public DefaultProxyRoutePlanner getDefaultRoutePlanner() {
        // 如果配置中心proxy设置不为空，则设置Http接口代理
        if (this.maps.containsKey("httpProxy") &&
                !this.maps.get("httpProxy").isEmpty()) {
            String url = this.maps.get("httpProxy");

            try {
                URI uri = new URIBuilder(url).build();
                HttpHost proxy = new HttpHost(uri.getHost(), uri.getPort());

                return new DefaultProxyRoutePlanner(proxy);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        // 如果配置中心proxy未设置或设置为空，则返回null
        return null;
    }

    /**
     * 设置简单代理 - by HttpHost
     * @param proxy
     * @return
     */
    public DefaultProxyRoutePlanner getDefaultRoutePlanner(HttpHost proxy) {
        if (proxy != null) {
            return new DefaultProxyRoutePlanner(proxy);
        }

        return null;
    }

    /**
     * @return
     * @throws URISyntaxException
     * @Funtion 设置自定义代理 - by配置中心
     */
    public HttpRoutePlanner getCustomRoutePlanner() {
        // 如果配置中心proxy设置不为空，则设置Http接口代理
        if (this.maps.containsKey("httpProxy") &&
                !this.maps.get("httpProxy").isEmpty()) {
            String url = this.maps.get("httpProxy");

            try {
                URI uri = new URIBuilder(url).build();
                String scheme = uri.getScheme();
                HttpHost proxy = new HttpHost(uri.getHost(), uri.getPort());

                HttpRoutePlanner routePlanner = (target, request, context) -> new HttpRoute(target, null, proxy,
                        scheme.equalsIgnoreCase(target.getSchemeName()));

                return routePlanner;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        // 如果配置中心proxy未设置或设置为空，则返回null
        return null;
    }

    /**
     * @return
     * @throws URISyntaxException
     * @Funtion 设置自定义代理 - by scheme and HttpHost
     */
    public HttpRoutePlanner getCustomRoutePlanner(String scheme, HttpHost proxy) {
        if (scheme != null &&
                !scheme.isEmpty() &&
                proxy != null) {
            HttpRoutePlanner routePlanner = (target, request, context) -> new HttpRoute(target, null, proxy,
                    scheme.equalsIgnoreCase(target.getSchemeName()));

            return routePlanner;
        }

        return null;
    }
}
