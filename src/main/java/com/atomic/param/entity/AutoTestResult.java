package com.atomic.param.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by dreamyao on 2017/3/15.
 */
public class AutoTestResult implements Serializable {

    private Integer id;

    /**
     * 项目Id
     */
    private Integer project_id;

    /**
     * 被测类名
     */
    private String class_name;

    /**
     * 被测方法名
     */
    private String methods_name;

    private String case_name;

    /**
     * 入参
     */
    private String methods_parameter;

    /**
     * 返回值
     */
    private String methods_return;

    /**
     * 预期返回值，值来至于excel中
     */
    private String expected_return;

    /**
     * 测试结果
     */
    private Integer is_pass;

    /**
     * 测试轮次数
     */
    private Integer round;

    private String run_author;

    /**
     * 创建时间
     */
    private Date create_time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProject_id() {
        return project_id;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getMethods_name() {
        return methods_name;
    }

    public void setMethods_name(String methods_name) {
        this.methods_name = methods_name;
    }

    public String getCase_name() {
        return case_name;
    }

    public void setCase_name(String case_name) {
        this.case_name = case_name;
    }

    public String getMethods_parameter() {
        return methods_parameter;
    }

    public void setMethods_parameter(String methods_parameter) {
        this.methods_parameter = methods_parameter;
    }

    public String getMethods_return() {
        return methods_return;
    }

    public void setMethods_return(String methods_return) {
        this.methods_return = methods_return;
    }

    public Integer getIs_pass() {
        return is_pass;
    }

    public void setIs_pass(Integer is_pass) {
        this.is_pass = is_pass;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public String getRun_author() {
        return run_author;
    }

    public void setRun_author(String run_author) {
        this.run_author = run_author;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public String getExpected_return() {
        return expected_return;
    }

    public void setExpected_return(String expected_return) {
        this.expected_return = expected_return;
    }
}
