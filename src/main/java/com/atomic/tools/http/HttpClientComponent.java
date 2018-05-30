package com.atomic.tools.http;

import com.atomic.exception.HttpInterfaceException;
import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Args;
import org.testng.Reporter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HttpClient类型转换工具类
 * @author Yangminhan
 * @version 1.0
 *          <p>
 *          Created by Yangminhan on 2017/6/1.
 */
final class HttpClientComponent {

    private static final HttpClientComponent INSTANCE = new HttpClientComponent();

    private HttpClientComponent() {
    }

    public static HttpClientComponent getInstance() {
        return INSTANCE;
    }

    /**
     * 类型转换：Map<String, Object> to List<NameValuePair>
     * @param mapPair
     */
    List<NameValuePair> getNameValuePairList(Map<String, Object> mapPair) {
        Args.notNull(mapPair, "List<NameValuePair>对象入参mapPair");
        List<NameValuePair> nameValuePairList = Lists.newArrayList();
        if (!mapPair.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iterMap = mapPair.entrySet().iterator();
            while (iterMap.hasNext()) {
                Map.Entry<String, Object> entry = iterMap.next();
                NameValuePair nameValuePair = new BasicNameValuePair(entry.getKey(),
                        entry.getValue().toString());
                nameValuePairList.add(nameValuePair);
            }
        }
        return nameValuePairList;
    }

    /**
     * 类型转换：Map<String, Object> to JSONString
     * @param jsonMap
     * @return
     */
    String getJSONString(Map<String, Object> jsonMap) throws Exception {
        Args.notNull(jsonMap, "JSON String对象入参mapPair");
        String jsonStr = "{}";
        if (!jsonMap.isEmpty()) {
            if (null != jsonMap.get("data")) {
                if (!jsonMap.get("data").toString().equals("")) {
                    jsonStr = jsonMap.get("data").toString();
                }
            } else {
                Reporter.log("[HttpClientComponent#getJSONString()]:{Post请求application/json媒体类型入参格式错误}");
                throw new HttpInterfaceException("Post请求application/json媒体类型入参格式错误,入参格式要求:Map<\"data\", \"{...}\">");
            }
        }
        return jsonStr;
    }

    /**
     * 类型转换：Map<String, Object> to byte[]
     * @param byteMap
     * @return
     * @throws Exception
     */
    byte[] getByteArray(Map<String, Object> byteMap) throws Exception {
        Args.notNull(byteMap, "入参Map对象");
        byte[] bytes = null;
        if (byteMap.size() == 1) {
            Iterator<Map.Entry<String, Object>> iterMap = byteMap.entrySet().iterator();
            while (iterMap.hasNext()) {
                Map.Entry<String, Object> entry = iterMap.next();
                if (entry.getValue() instanceof Byte) {
                    bytes = (byte[]) entry.getValue();
                } else {
                    Reporter.log("[HttpClientComponent#getByteArray()]:{Post请求application/octet-stream媒体类型入参格式错误}");
                    throw new HttpInterfaceException("Post请求ContentType为application/octet-stream时,入参为已序列化的byte[]对象");
                }
            }
        } else {
            Reporter.log("[HttpClientComponent#getByteArray()]:{Post请求application/octet-stream媒体类型入参格式错误}");
            throw new HttpInterfaceException("Post请求ContentType为application/octet-stream时,入参Map<String, Object>有且仅有1个K-V键值对");
        }
        return bytes;
    }

    /**
     * 类型转换：Map<String, Object> to List<Header>
     * @param mapHeader
     * @return
     */
    List<Header> getHeaderList(Map<String, Object> mapHeader) {
        Args.notNull(mapHeader, "");
        List<Header> headers = new ArrayList<Header>();
        if (!mapHeader.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iterMap = mapHeader.entrySet().iterator();
            while (iterMap.hasNext()) {
                Map.Entry<String, Object> entry = iterMap.next();
                headers.add(new BasicHeader(entry.getKey(), entry.getValue().toString()));
            }
        }
        return headers;
    }

    /**
     * 类型转换：InputStream to String
     * @param instream
     * @return
     * @throws IOException
     */
    String inStreamToString(InputStream instream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "utf-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        instream.close();
        return new String(sb);
    }

    /**
     * 类型转换：InputStream to String
     * @param instream
     * @param l
     * @return
     */
    byte[] inStreamToByteArray(InputStream instream, long l) throws IOException {
        byte[] result = new byte[4 * (int) l];
        byte[] buffer = new byte[1024];
        int iLength = 0;
        int itemp = 0;
        instream.read(buffer);
        while ((itemp = instream.read(buffer)) != -1) {
            for (int i = 0; i < 1024; i++) {
                result[iLength + i] = buffer[i];
            }
            iLength += itemp;
        }
        return result;
    }

    /**
     * 序列化：Object to byte[]
     * @param obj
     * @return
     */
    byte[] serializeByteArray(Object obj) {
        Args.notNull(obj, "Object对象");

        byte[] bytes = null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(byteArrayOutputStream);

            outputStream.writeObject(obj);
            outputStream.flush();

            bytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }

    /**
     * 反序列化：bytes[] to Object
     * @param bytes
     * @return
     */
    Object deserializeObject(byte[] bytes) {
        Args.notNull(bytes, "byte[] ");

        Object object = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
            object = inputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return object;
    }

    /**
     * convert string to unicode
     * @param str
     * @return
     */
    String convert2Unicode(String str) {
        StringBuffer unicodeStr = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            // 取出每一个字符
            char c = str.charAt(i);
            // 转换为unicode
            unicodeStr.append("\\u" + Integer.toHexString(c));
        }
        return unicodeStr.toString();
    }

    /**
     * revert unicode to gb string
     * @param unicodeStr
     * @return
     */
    String revert2GBString(String unicodeStr) {
        char aChar;
        int len = unicodeStr.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = unicodeStr.charAt(x++);
            if (aChar == '\\') {
                aChar = unicodeStr.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = unicodeStr.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';

                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }
}
