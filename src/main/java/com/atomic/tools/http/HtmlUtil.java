package com.atomic.tools.http;

import com.atomic.exception.UtilException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * HTML工具类
 * @author dreamyao
 * @version 1.0.0
 */
public final class HtmlUtil {

    private static final char C_BACKSLASH = '\\';
    private static final char C_DELIM_START = '{';
    private static final String EMPTY = "";
    private static final String HTML_NBSP = "&nbsp;";
    private static final String HTML_AMP = "&amp";
    private static final String HTML_QUOTE = "&quot;";
    private static final String HTML_LT = "&lt;";
    private static final String HTML_GT = "&gt;";
    private static final String EMPTY_JSON = "{}";
    private static final String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";
    /** UTF-8 */
    private static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;
    private static final char[][] TEXT = new char[64][];

    static {
        for (int i = 0; i < 64; i++) {
            TEXT[i] = new char[]{(char) i};
        }

        // 单引号 ('&apos;' doesn't work - it is not by the w3 specs)
        TEXT['\''] = "&#039;".toCharArray();
        // 双引号
        TEXT['"'] = HTML_QUOTE.toCharArray();
        // &符
        TEXT['&'] = HTML_AMP.toCharArray();
        // 小于号
        TEXT['<'] = HTML_LT.toCharArray();
        // 大于号
        TEXT['>'] = HTML_GT.toCharArray();
    }

    private HtmlUtil() {
    }

    /**
     * 还原被转义的HTML特殊字符
     * @param htmlStr 包含转义符的HTML内容
     * @return 转换后的字符串
     */
    public static String restoreEscaped(String htmlStr) {
        if (Objects.isNull(htmlStr) || htmlStr.length() == 0) {
            return htmlStr;
        }
        return htmlStr
                .replace("&#39;", "'")
                .replace(HTML_LT, "<")
                .replace(HTML_GT, ">")
                .replace(HTML_AMP, "&")
                .replace(HTML_QUOTE, "\"")
                .replace(HTML_NBSP, " ");
    }

    /**
     * 转义文本中的HTML字符为安全的字符，以下字符被转义：
     * <ul>
     * <li>' with &amp;#039; (&amp;apos; doesn't work in HTML4)</li>
     * <li>" with &amp;quot;</li>
     * <li>&amp; with &amp;amp;</li>
     * <li>&lt; with &amp;lt;</li>
     * <li>&gt; with &amp;gt;</li>
     * </ul>
     * @param text 被转义的文本
     * @return 转义后的文本
     */
    public static String encode(String text) {
        return encode(text, TEXT);
    }

    /**
     * 清除所有HTML标签
     * @param content 文本
     * @return 清除标签后的文本
     */
    public static String cleanHtmlTag(String content) {
        return content.replaceAll(RE_HTML_MARK, "");
    }

    /**
     * 清除指定HTML标签和被标签包围的内容
     * 不区分大小写
     * @param content  文本
     * @param tagNames 要清除的标签
     * @return 去除标签后的文本
     */
    public static String removeHtmlTag(String content, String... tagNames) {
        return removeHtmlTag(content, true, tagNames);
    }

    /**
     * 清除指定HTML标签，不包括内容
     * 不区分大小写
     * @param content  文本
     * @param tagNames 要清除的标签
     * @return 去除标签后的文本
     */
    public static String unwrapHtmlTag(String content, String... tagNames) {
        return removeHtmlTag(content, false, tagNames);
    }

    /**
     * 清除指定HTML标签
     * 不区分大小写
     * @param content        文本
     * @param withTagContent 是否去掉被包含在标签中的内容
     * @param tagNames       要清除的标签
     * @return 去除标签后的文本
     */
    public static String removeHtmlTag(String content, boolean withTagContent, String... tagNames) {
        String regex1 = null;
        String regex2 = null;
        for (String tagName : tagNames) {
            if (Objects.isNull(tagName) || tagName.length() == 0) {
                continue;
            }
            tagName = tagName.trim();
            //(?i)表示其后面的表达式忽略大小写
            regex1 = format("(?i)<{}\\s?[^>]*?/>", tagName);
            if (withTagContent) {
                //标签及其包含内容
                regex2 = format("(?i)(?s)<{}\\s*?[^>]*?>.*?</{}>", tagName, tagName);
            } else {
                //标签不包含内容
                regex2 = format("(?i)<{}\\s*?[^>]*?>|</{}>", tagName, tagName);
            }
            content = content
                    .replaceAll(regex1, EMPTY)                                    //自闭标签小写
                    .replaceAll(regex2, EMPTY);                                    //非自闭标签小写
        }
        return content;
    }

    public static String format(CharSequence template, Object... params) {
        if (null == template) {
            return null;
        }
        if (params.length == 0 || template.length() == 0) {
            return template.toString();
        }
        return format(template.toString(), params);
    }

    /**
     * 格式化字符串<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") =》 this is a for b<br>
     * 转义{}： 	format("this is \\{} for {}", "a", "b") =》 this is \{} for a<br>
     * 转义\：		format("this is \\\\{} for {}", "a", "b") =》 this is \a for b<br>
     * @param strPattern 字符串模板
     * @param argArray   参数列表
     * @return 结果
     */
    public static String format(final String strPattern, final Object... argArray) {
        if (Objects.isNull(strPattern) || strPattern.length() == 0 || argArray.length == 0) {
            return strPattern;
        }
        final int strPatternLength = strPattern.length();
        //初始化定义好的长度以获得更好的性能
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;//记录已经处理到的位置
        int delimIndex;//占位符所在位置
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf(EMPTY_JSON, handledPosition);
            if (delimIndex == -1) {//剩余部分无占位符
                if (handledPosition == 0) { //不带占位符的模板直接返回
                    return strPattern;
                } else { //字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
                    sbuf.append(strPattern, handledPosition, strPatternLength);
                    return sbuf.toString();
                }
            } else {
                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == C_BACKSLASH) {//转义符
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == C_BACKSLASH) {//双转义符
                        //转义符之前还有一个转义符，占位符依旧有效
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(utf8Str(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    } else {
                        //占位符被转义
                        argIndex--;
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(C_DELIM_START);
                        handledPosition = delimIndex + 1;
                    }
                } else {//正常占位符
                    sbuf.append(strPattern, handledPosition, delimIndex);
                    sbuf.append(utf8Str(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        //加入最后一个占位符后所有的字符
        sbuf.append(strPattern, handledPosition, strPattern.length());
        return sbuf.toString();
    }

    /**
     * 将对象转为字符串<br>
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     * @param obj 对象
     * @return 字符串
     */
    public static String utf8Str(Object obj) {
        return str(obj, CHARSET_UTF_8);
    }

    /**
     * 将对象转为字符串<br>
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     * @param obj     对象
     * @param charset 字符集
     * @return 字符串
     */
    public static String str(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[]) {
            return str((byte[]) obj, charset);
        } else if (obj instanceof Byte[]) {
            return str((Byte[]) obj, charset);
        } else if (obj instanceof ByteBuffer) {
            return str((ByteBuffer) obj, charset);
        } else if (isArray(obj)) {
            return toString(obj);
        }
        return obj.toString();
    }

    /**
     * 解码字节码
     * @param data    字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String str(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }
        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * 解码字节码
     * @param data    字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String str(Byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }
        byte[] bytes = new byte[data.length];
        Byte dataByte;
        for (int i = 0; i < data.length; i++) {
            dataByte = data[i];
            bytes[i] = (null == dataByte) ? -1 : dataByte.byteValue();
        }
        return str(bytes, charset);
    }

    /**
     * 将编码的byteBuffer数据转换为字符串
     * @param data    数据
     * @param charset 字符集，如果为空使用当前系统字符集
     * @return 字符串
     */
    public static String str(ByteBuffer data, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    /**
     * 数组或集合转String
     * @param obj 集合或数组对象
     * @return 数组字符串，与集合转字符串格式相同
     */
    public static String toString(Object obj) {
        if (null == obj) {
            return null;
        }
        if (isArray(obj)) {
            try {
                return Arrays.deepToString((Object[]) obj);
            } catch (Exception e) {
                final String className = obj.getClass().getComponentType().getName();
                switch (className) {
                    case "long":
                        return Arrays.toString((long[]) obj);
                    case "int":
                        return Arrays.toString((int[]) obj);
                    case "short":
                        return Arrays.toString((short[]) obj);
                    case "char":
                        return Arrays.toString((char[]) obj);
                    case "byte":
                        return Arrays.toString((byte[]) obj);
                    case "boolean":
                        return Arrays.toString((boolean[]) obj);
                    case "float":
                        return Arrays.toString((float[]) obj);
                    case "double":
                        return Arrays.toString((double[]) obj);
                    default:
                        throw new UtilException(e);
                }
            }
        }
        return obj.toString();
    }

    /**
     * 对象是否为数组对象
     * @param obj 对象
     * @return 是否为数组对象，如果为{@code null} 返回false
     */
    public static boolean isArray(Object obj) {
        if (null == obj) {
            return false;
        }
        return obj.getClass().isArray();
    }

    /**
     * 去除HTML标签中的属性
     * @param content 文本
     * @param attrs   属性名（不区分大小写）
     * @return 处理后的文本
     */
    public static String removeHtmlAttr(String content, String... attrs) {
        String regex = null;
        for (String attr : attrs) {
            regex = format("(?i)\\s*{}=([\"']).*?\\1", attr);
            content = content.replaceAll(regex, EMPTY);
        }
        return content;
    }

    /**
     * 去除指定标签的所有属性
     * @param content  内容
     * @param tagNames 指定标签
     * @return 处理后的文本
     */
    public static String removeAllHtmlAttr(String content, String... tagNames) {
        String regex = null;
        for (String tagName : tagNames) {
            regex = format("(?i)<{}[^>]*?>", tagName);
            content.replaceAll(regex, format("<{}>", tagName));
        }
        return content;
    }

    /**
     * Encoder
     * @param text  被编码的文本
     * @param array 特殊字符集合
     * @return 编码后的字符
     */
    private static String encode(String text, char[][] array) {
        int len;
        if ((text == null) || ((len = text.length()) == 0)) {
            return EMPTY;
        }
        StringBuilder buffer = new StringBuilder(len + (len >> 2));
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c < 64) {
                buffer.append(array[c]);
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    /**
     * 过滤HTML文本，防止XSS攻击
     * @param htmlContent HTML内容
     * @return 过滤后的内容
     */
    public static String filter(String htmlContent) {
        return new HTMLFilter().filter(htmlContent);
    }
}
