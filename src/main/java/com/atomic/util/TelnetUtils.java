package com.atomic.util;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * 通用Telnet协议处理工具类
 * @author dreamyao
 * @version 1.0.0
 *          Created by dreamyao on 2017/7/6.
 */
public class TelnetUtils {

    private String prompt = ">";    //结束标识字符串,Windows中是>,Linux中是#
    private char promptChar = '>';    //结束标识字符
    private TelnetClient telnet;
    private InputStream in;        // 输入流,接收返回信息
    private PrintStream out;    // 向服务器写入 命令

    /**
     * @param termType 协议类型：VT100、VT52、VT220、VTNT、ANSI
     * @param prompt   结果结束标识
     */
    public TelnetUtils(String termType, String prompt) {
        telnet = new TelnetClient(termType);
        setPrompt(prompt);
    }

    public TelnetUtils(String termType) {
        telnet = new TelnetClient(termType);
    }

    public TelnetUtils() {
        telnet = new TelnetClient();
    }

    public static void main(String[] args) {
        TelnetUtils telnet = new TelnetUtils("VT220", ">");    //Windows,用VT220,否则会乱码
        telnet.login("192.168.2.19", 23, "administrator", "123456");
        String rs = telnet.sendCommand("ipconfig");
        try {
            rs = new String(rs.getBytes("ISO-8859-1"), "GBK");    //转一下编码
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(rs);
    }

    /**
     * 登录到目标主机
     * @param ip
     * @param port
     * @param username
     * @param password
     */
    public void login(String ip, int port, String username, String password) {
        try {
            telnet.connect(ip, port);
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());
            readUntil("login:");
            write(username);
            readUntil("password:");
            write(password);
            String rs = readUntil(null);
            if (rs != null && rs.contains("Login Failed")) {
                throw new RuntimeException("登录失败");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取分析结果
     * @param pattern 匹配到该字符串时返回结果
     * @return
     */
    public String readUntil(String pattern) {
        StringBuilder sb = new StringBuilder();
        try {
            char lastChar = (char) -1;
            boolean flag = pattern != null && pattern.length() > 0;
            if (flag) {
                lastChar = pattern.charAt(pattern.length() - 1);
            }
            char ch;
            int code;
            while ((code = in.read()) != -1) {
                ch = (char) code;
                sb.append(ch);
                //匹配到结束标识时返回结果
                if (flag) {
                    if (ch == lastChar && sb.toString().endsWith(pattern)) {
                        return sb.toString();
                    }
                } else {
                    //如果没指定结束标识,匹配到默认结束标识字符时返回结果
                    if (ch == promptChar)
                        return sb.toString();
                }
                //登录失败时返回结果
                if (sb.toString().contains("Login Failed")) {
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 发送命令
     * @param value
     */
    public void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送命令,返回执行结果
     * @param command
     * @return
     */
    public String sendCommand(String command) {
        try {
            write(command);
            return readUntil(prompt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭telnet连接
     */
    public void distinct() {
        try {
            if (telnet != null && !telnet.isConnected())
                telnet.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置协议类型
     * @param prompt
     */
    public void setPrompt(String prompt) {
        if (prompt != null) {
            this.prompt = prompt;
            this.promptChar = prompt.charAt(prompt.length() - 1);
        }
    }
}
