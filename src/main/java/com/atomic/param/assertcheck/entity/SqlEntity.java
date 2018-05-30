package com.atomic.param.assertcheck.entity;

/**
 * 断言所需查询语句
 */
public class SqlEntity {

    /**
     * 数据源
     */
    private String dataSource;
    /**
     * 查询的表
     */
    private String table;
    /**
     * 字段，逗号分隔
     */
    private String fields;
    /**
     * 条件
     */
    private String condition;
    /**
     * 直接写完整的sql
     */
    private String sql;

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
