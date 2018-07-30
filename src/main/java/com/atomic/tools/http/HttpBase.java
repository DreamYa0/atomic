package com.atomic.tools.http;

import org.apache.commons.collections4.CollectionUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * http基类
 * @param <T> 子类类型，方便链式编程
 * @author dreamyao
 */
@SuppressWarnings("unchecked")
public abstract class HttpBase<T> {

    /** HTTP/1.0 */
    public static final String HTTP_1_0 = "HTTP/1.0";
    /** HTTP/1.1 */
    public static final String HTTP_1_1 = "HTTP/1.1";
    private static final String CRLF = "\r\n";
    private static final String EMPTY = "";
    /** UTF-8 */
    private static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;
    /** 存储头信息 */
    protected Map<String, List<String>> headers = new HashMap<String, List<String>>();
    /** 编码 */
    protected Charset charset = CHARSET_UTF_8;
    /** http版本 */
    protected String httpVersion = HTTP_1_1;
    /** 存储主体 */
    protected String body;

    /**
     * 当给定字符串为null时，转换为Empty
     * @param str 被转换的字符串
     * @return 转换后的字符串
     */
    private static String nullToEmpty(CharSequence str) {
        return nullToDefault(str, EMPTY);
    }

    /**
     * 如果字符串是<code>null</code>，则返回指定默认字符串，否则返回字符串本身。
     * <pre>
     * nullToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * nullToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;&quot;
     * nullToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;  &quot;
     * nullToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     * @param str        要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    private static String nullToDefault(CharSequence str, String defaultStr) {
        return (str == null) ? defaultStr : str.toString();
    }

    private static StringBuilder builder() {
        return new StringBuilder();
    }

    /**
     * 创建StringBuilder对象
     * @param capacity 初始大小
     * @return StringBuilder对象
     */
    public static StringBuilder builder(int capacity) {
        return new StringBuilder(capacity);
    }

    /**
     * 创建StringBuilder对象
     * @param strs 初始字符串列表
     * @return StringBuilder对象
     */
    public static StringBuilder builder(CharSequence... strs) {
        final StringBuilder sb = new StringBuilder();
        for (CharSequence str : strs) {
            sb.append(str);
        }
        return sb;
    }

    /**
     * 根据name获取头信息
     * @param name Header名
     * @return Header值
     */
    public String header(String name) {
        final List<String> values = headerList(name);
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.get(0);
    }

    /**
     * 根据name获取头信息列表
     * @param name Header名
     * @return Header值
     * @since 3.1.1
     */
    public List<String> headerList(String name) {
        if (Objects.isNull(name) || name.length() == 0) {
            return null;
        }
        return headers.get(name.trim());
    }

    /**
     * 根据name获取头信息
     * @param name Header名
     * @return Header值
     */
    public String header(Header name) {
        if (null == name) {
            return null;
        }
        return header(name.toString());
    }

    /**
     * 设置一个header
     * 如果覆盖模式，则替换之前的值，否则加入到值列表中
     * @param name       Header名
     * @param value      Header值
     * @param isOverride 是否覆盖已有值
     * @return T 本身
     */
    public T header(String name, String value, boolean isOverride) {
        if (null != name && null != value) {
            final List<String> values = headers.get(name.trim());
            if (isOverride || CollectionUtils.isEmpty(values)) {
                final List<String> valueList = new ArrayList<>();
                valueList.add(value);
                headers.put(name.trim(), valueList);
            } else {
                values.add(value.trim());
            }
        }
        return (T) this;
    }

    /**
     * 设置一个header
     * 如果覆盖模式，则替换之前的值，否则加入到值列表中
     * @param name       Header名
     * @param value      Header值
     * @param isOverride 是否覆盖已有值
     * @return T 本身
     */
    public T header(Header name, String value, boolean isOverride) {
        return header(name.toString(), value, isOverride);
    }

    /**
     * 设置一个header
     * 覆盖模式，则替换之前的值
     * @param name  Header名
     * @param value Header值
     * @return T 本身
     */
    public T header(Header name, String value) {
        return header(name.toString(), value, true);
    }

    /**
     * 设置一个header
     * 覆盖模式，则替换之前的值
     * @param name  Header名
     * @param value Header值
     * @return T 本身
     */
    public T header(String name, String value) {
        return header(name, value, true);
    }

    /**
     * 设置请求头
     * 不覆盖原有请求头
     * @param headers 请求头
     * @return this
     */
    public T header(Map<String, List<String>> headers) {
        if (headers.isEmpty()) {
            return (T) this;
        }
        String name;
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            name = entry.getKey();
            for (String value : entry.getValue()) {
                this.header(name, nullToEmpty(value), false);
            }
        }
        return (T) this;
    }

    /**
     * 移除一个头信息
     * @param name Header名
     * @return this
     */
    public T removeHeader(String name) {
        if (name != null) {
            headers.remove(name.trim());
        }
        return (T) this;
    }

    /**
     * 移除一个头信息
     * @param name Header名
     * @return this
     */
    public T removeHeader(Header name) {
        return removeHeader(name.toString());
    }

    /**
     * 获取headers
     * @return Headers Map
     */
    public Map<String, List<String>> headers() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * 返回http版本
     * @return String
     */
    public String httpVersion() {
        return httpVersion;
    }

    /**
     * 设置http版本
     * @param httpVersion Http版本，{@link HttpBase#HTTP_1_0}，{@link HttpBase#HTTP_1_1}
     * @return this
     */
    public T httpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return (T) this;
    }

    /**
     * 返回字符集
     * @return 字符集
     */
    public String charset() {
        return charset.name();
    }

    /**
     * 设置字符集
     * @param charset 字符集
     * @return T 自己
     */
    public T charset(String charset) {
        if (Objects.isNull(charset) || charset.length() == 0) {
            this.charset = Charset.forName(charset);
        }
        return (T) this;
    }

    /**
     * 设置字符集
     * @param charset 字符集
     * @return T 自己
     */
    public T charset(Charset charset) {
        if (null != charset) {
            this.charset = charset;
        }
        return (T) this;
    }

    @Override
    public String toString() {
        StringBuilder sb = builder();
        sb.append("Request Headers: ").append(CRLF);
        for (Entry<String, List<String>> entry : this.headers.entrySet()) {
            sb.append("    ").append(entry).append(CRLF);
        }
        sb.append("Request Body: ").append(CRLF);
        sb.append("    ").append(this.body).append(CRLF);
        return sb.toString();
    }
}
