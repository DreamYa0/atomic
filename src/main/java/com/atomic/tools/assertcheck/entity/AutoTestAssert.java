package com.atomic.tools.assertcheck.entity;

import java.util.Date;

/**
 * Created by dreamyao on 2017/6/18.
 */
public class AutoTestAssert {

    private Integer id;
    /** 被测方法名称 */
    private String method_name;
    /** 被测方法入参 */
    private String method_param;
    /** 被测方法返回值 */
    private String method_return;
    /** 创建时间 */
    private Date create_time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMethod_name() {
        return method_name;
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }

    public String getMethod_param() {
        return method_param;
    }

    public void setMethod_param(String method_param) {
        this.method_param = method_param;
    }

    public String getMethod_return() {
        return method_return;
    }

    public void setMethod_return(String method_return) {
        this.method_return = method_return;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
