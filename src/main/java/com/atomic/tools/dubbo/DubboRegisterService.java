package com.atomic.tools.dubbo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * ___====-_  _-====___
 * _--^^^#####//      \\#####^^^--_
 * _-^##########// (    ) \\##########^-_
 * -############//  |\^^/|  \\############-
 * _/############//   (@::@)   \\############\_
 * /#############((     \\//     ))#############\
 * -###############\\    (oo)    //###############-
 * -#################\\  / VV \  //#################-
 * -###################\\/      \//###################-
 * _#/|##########/\######(   /\   )######/\##########|\#_
 * |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 * `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 * `   `  `      `   / | |  | | \   '      '  '   '
 * (  | |  | |  )
 * __\ | |  | | /__
 * (vvv(VVV)(VVV)vvv)
 */
public class DubboRegisterService {

    private String serviceName;
    private String postDomain;
    private String postPort;
    private Map<String, String> params = Maps.newHashMap();
    private List<String> methods = Lists.newArrayList();
    private String registerUrl;


    public DubboRegisterService(String registerUrl) {
        this.registerUrl = registerUrl;
        init();
    }


    private void init() {
        try {
            registerUrl = URLDecoder.decode(registerUrl, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = registerUrl;
        //解析url
        String strs[] = url.split("\\?");
        //解析头部
        String prefixUrl = strs[0];
        String prefix = "META_INF.dubbo://";
        if (prefixUrl.startsWith(prefix)) {
            prefixUrl = prefixUrl.substring(prefix.length());
        }
        String[] preStrs = prefixUrl.split("/");
        postDomain = preStrs[0].split(":")[0];
        serviceName = preStrs[1];
        //解析键值对
        List<NameValuePair> pairs = URLEncodedUtils.parse(registerUrl, Charset.forName("UTF-8"));
        for (NameValuePair pair : pairs) {
            params.put(pair.getName(), pair.getValue());
        }
        postPort = params.get("application.version");
        String methodStr = params.get("methods");
        for (String s : methodStr.split(",")) {
            methods.add(s);
        }
    }

    public Map<String, String> getParams() {
        return params;
    }

    public List<String> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        return "DubboService{" +
                "serviceName='" + serviceName + '\'' +
                ", postDomain='" + postDomain + '\'' +
                ", postPort='" + postPort + '\'' +
                ", params=" + params +
                ", methods=" + methods +
                ", registerUrl='" + registerUrl + '\'' +
                '}';
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPostDomain() {
        return postDomain;
    }

    public String getPostPort() {
        return postPort;
    }

}
