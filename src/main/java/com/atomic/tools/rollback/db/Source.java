package com.atomic.tools.rollback.db;

/**
 * 表示连接到数据库的信息的源。它包含了数据库的url、用户和密码
 */
public class Source {

    private final String url;

    private final String user;

    private final String password;

    public Source(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
