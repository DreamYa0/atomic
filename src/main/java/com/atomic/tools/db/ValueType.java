package com.atomic.tools.db;

public enum ValueType {

    BYTES,

    BOOLEAN,

    TEXT,

    DATE,

    TIME,

    DATE_TIME,
    /**
     * Number 类型 (INT, SMALLINT, TINYINT, BIGINT, REAL or DECIMAL column).
     */
    NUMBER,
    /**
     * UUID type.
     */
    UUID,
    /**
     * 不确定类型:例如null值。
     */
    NOT_IDENTIFIED;

    /**
     * 返回的类型可能与实际值(数据)的一个预期值。
     * @param expected 预期值
     * @return 可能的类型的实际值
     */
    public static ValueType[] getPossibleTypesForComparison(Object expected) {
        if (expected instanceof byte[]) {
            return new ValueType[]{BYTES};
        }
        if (expected instanceof Boolean) {
            return new ValueType[]{BOOLEAN};
        }
        if (expected instanceof String) {
            return new ValueType[]{ValueType.TEXT, ValueType.NUMBER, ValueType.DATE, ValueType.TIME, ValueType.DATE_TIME,
                    ValueType.UUID};
        }
        if (expected instanceof DateValue) {
            return new ValueType[]{ValueType.DATE, ValueType.DATE_TIME};
        }
        if (expected instanceof TimeValue) {
            return new ValueType[]{ValueType.TIME};
        }
        if (expected instanceof DateTimeValue) {
            return new ValueType[]{DATE_TIME};
        }
        if (expected instanceof Number) {
            return new ValueType[]{NUMBER};
        }
        if (expected instanceof java.util.UUID) {
            return new ValueType[]{UUID};
        }
        return new ValueType[]{NOT_IDENTIFIED};
    }
}
