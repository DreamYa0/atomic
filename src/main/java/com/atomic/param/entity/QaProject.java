package com.atomic.param.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by dreamyao on 2017/3/15.
 */
public class QaProject implements Serializable {

    private Integer id;

    /**
     * 项目名称
     */
    private String project_name;

    /**
     * 项目状态 0：禁用 1：启用
     */
    private Integer project_status;

    private String author;

    /**
     * 项目创建时间
     */
    private Date create_time;

    /**
     * 项目更新时间
     */
    private Date update_time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public Integer getProject_status() {
        return project_status;
    }

    public void setProject_status(Integer project_status) {
        this.project_status = project_status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }
}
