package com.atomic.tools.http;

import com.atomic.exception.HttpInterfaceException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.testng.Reporter;

import java.util.List;

/**
 * HttpClient上下文工具类
 * @author Yangminhan
 * @version 1.0
 *          <p>
 *          Created by Yangminhan on 2017/6/1.
 */
public class HttpCltContext {

    private static final HttpCltContext INSTANCE = new HttpCltContext();
    private static HttpClientContext context;
    private static List<Cookie> cookieList;

    private HttpCltContext() {
    }

    public static HttpCltContext getInstance() {
        return INSTANCE;
    }

    /**
     * 返回HTTP上下文
     * @return
     */
    public HttpClientContext getClientContext() {
        return context;
    }

    /**
     * 初始化HTTP上下文 - HttpClientContext
     */
    public void setClientContext() {
        HttpContext httpContext = new BasicHttpContext();
        context = context.adapt(httpContext);
    }

    /**
     * 返回HTTP Cookie List
     * @return
     */
    List<Cookie> getCookieList() {
        return cookieList;
    }

    /**
     * 从HTTP上下文获取Cookie List
     * @param context
     */
    void setCookieList(HttpClientContext context) {
        Args.notNull(context, "HttpClientContext");
        cookieList = null;
        CookieStore cookieStore = context.getCookieStore();
        cookieList = cookieStore.getCookies();
    }

    /**
     * 返回指定Cookie Name对应的Value
     * @param cookies
     * @param cookieName
     * @return
     */
    String getCookieValue(final List<Cookie> cookies, final String cookieName) {
        Args.notEmpty(cookies, "HTTP Cookie List");
        Args.notEmpty(cookieName, "Query Cookie Name");
        String cookieValue = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                cookieValue = cookie.getValue();
            }
        }
        return cookieValue;
    }

    /**
     * 设置Http上下文Cookie
     * @param context
     * @param cookies
     * @throws Exception
     */
    void setCookieStore(HttpClientContext context, List<Cookie> cookies) throws Exception {
        Args.notNull(context, "HttpClientContext");
        Args.notNull(cookies, "Cookie");
        if (cookies.size() == 0) {
            Reporter.log("[HttpCltContext#setCookieStore()]:{List<Cookie> size not be 0!}");
            throw new HttpInterfaceException("List<Cookie> size not be 0!");
        }
        CookieStore cookieStore = new BasicCookieStore();
        for (Cookie cookie : cookies) {
            cookieStore.addCookie(cookie);
        }
        context.setCookieStore(cookieStore);
    }
}
