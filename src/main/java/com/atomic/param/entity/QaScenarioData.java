package com.atomic.param.entity;

import java.util.Date;

/**
 * Created by dreamyao on 2017/6/25.
 */
public class QaScenarioData {

    private Integer id;
    private String method_name;
    private String method_parameter;
    private String method_return;
    private Integer case_index;
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

    public String getMethod_parameter() {
        return method_parameter;
    }

    public void setMethod_parameter(String method_parameter) {
        this.method_parameter = method_parameter;
    }

    public String getMethod_return() {
        return method_return;
    }

    public void setMethod_return(String method_return) {
        this.method_return = method_return;
    }

    public Integer getCase_index() {
        return case_index;
    }

    public void setCase_index(Integer case_index) {
        this.case_index = case_index;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
