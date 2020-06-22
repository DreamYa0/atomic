package com.atomic.tools.assertcheck.entity;

/**
 * 断言项
 */
public class AssertItem {
    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 比较方式，对应 CompareType 枚举类
     */
    private int compareType;

    /**
     * 新值
     */
    private String newValue;

    /**
     * 旧值的sql
     */
    private SqlEntity oldSqlEntity;

    /**
     * 新值的sql
     */
    private SqlEntity newSqlEntity;

    /**
     * 新值比旧值大于或小于某个具体的值
     */
    private int fixedValue;

    /**
     * 断言方式，对应 AssertType 枚举类
     */
    private int assertType;

    public static SqlEntity createSqlEntity(String dataSource, String table, String fields, String condition) {
        SqlEntity sqlEntity = new SqlEntity();
        sqlEntity.setDataSource(dataSource);
        sqlEntity.setTable(table);
        sqlEntity.setFields(fields);
        sqlEntity.setCondition(condition);
        return sqlEntity;
    }

    public static SqlEntity createSqlEntity(String dataSource, String sql) {
        SqlEntity sqlEntity = new SqlEntity();
        sqlEntity.setDataSource(dataSource);
        sqlEntity.setSql(sql);
        return sqlEntity;
    }

    public int getAssertType() {
        return assertType;
    }

    public void setAssertType(int assertType) {
        this.assertType = assertType;
    }

    public int getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(int fixedValue) {
        this.fixedValue = fixedValue;
    }

    //	/**
//	 * 查询结果是单个还是列表
//	 */
//	private int valueType;
//
//	public int getValueType() {
//		return valueType;
//	}
//
//	public void setValueType(int valueType) {
//		this.valueType = valueType;
//	}

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public int getCompareType() {
        return compareType;
    }

    public void setCompareType(int compareType) {
        this.compareType = compareType;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public SqlEntity getOldSqlEntity() {
        return oldSqlEntity;
    }

    public void setOldSqlEntity(SqlEntity oldSqlEntity) {
        this.oldSqlEntity = oldSqlEntity;
    }

    public SqlEntity getNewSqlEntity() {
        return newSqlEntity;
    }

    public void setNewSqlEntity(SqlEntity newSqlEntity) {
        this.newSqlEntity = newSqlEntity;
    }
}
