package com.atomic.tools.db;

import com.atomic.tools.db.lettercase.LetterCase;
import com.atomic.tools.db.lettercase.WithColumnLetterCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

public class Value implements DbElement, WithColumnLetterCase {

    /**
     * 列名
     */
    private final String columnName;
    /**
     * 值
     */
    private final Object value;

    /**
     * 值的类型
     */
    private final ValueType valueType;
    /**
     * 列的字母大小写。
     * @since 1.1.0
     */
    private final LetterCase columnLetterCase;

    /**
     * Constructor.
     * @param columnName       列的名称
     * @param value            值
     * @param columnLetterCase 列的名称的大小写
     */
    Value(String columnName, Object value, LetterCase columnLetterCase) {
        this.columnName = columnName;
        this.value = value;
        this.columnLetterCase = columnLetterCase;
        valueType = getType(value);
    }

    /**
     * 返回空值
     * lettercase
     * AbstractDbData
     * AbstractDbElement
     * Change
     * Changes
     * ChangeType
     * Column
     * DataSourceWithLetterCase
     * DataType
     * DateTimeValue
     * DateValue
     * DateValueContainer
     * DbElement
     * Request
     * Row
     * Source
     * SourceWithLetterCase
     * Table
     * TimeValue
     * Value
     * ValueType
     * @param columnName       列名
     * @param columnLetterCase 列名大小写
     * @return 返回空值
     */
    public static Value getNullValue(String columnName, LetterCase columnLetterCase) {
        return new Value(columnName, null, columnLetterCase);
    }

    /**
     * 返回实际值的类型(数据)。
     * @param object 实际的对象中包含的值。
     * @return 实际值的类型
     */
    static ValueType getType(Object object) {
        if (object instanceof byte[]) {
            return ValueType.BYTES;
        }
        if (object instanceof Boolean) {
            return ValueType.BOOLEAN;
        }
        if (object instanceof String
                || object instanceof Character) {

            return ValueType.TEXT;
        }
        if (object instanceof Date) {
            return ValueType.DATE;
        }
        if (object instanceof Time) {
            return ValueType.TIME;
        }
        if (object instanceof Timestamp) {
            return ValueType.DATE_TIME;
        }
        if (object instanceof UUID) {
            return ValueType.UUID;
        }
        if (object instanceof Byte
                || object instanceof Short
                || object instanceof Integer
                || object instanceof Long
                || object instanceof Float
                || object instanceof Double
                || object instanceof BigDecimal
                || object instanceof BigInteger) {

            return ValueType.NUMBER;
        }
        return ValueType.NOT_IDENTIFIED;
    }

    @Override
    public LetterCase getColumnLetterCase() {
        return columnLetterCase;
    }

    /**
     * 返回,列的名称。
     * @return 列的名称
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 返回,值。
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     * 返回,值的类型
     * @return 值的类型
     */
    public ValueType getValueType() {
        return valueType;
    }

    /**
     * 返回,值的类型的代表性
     * @return 类型的值的表示
     */
    public String getValueTypeRepresentation() {
        if (value == null || valueType != ValueType.NOT_IDENTIFIED) {
            return valueType.toString();
        }
        return valueType + " : " + value.getClass();
    }

    /**
     * 返回,如果任何类型的值和对象之间的比较是有可能的参数
     * @param object 潜在的对象进行比较
     * @return
     */
    public boolean isComparisonPossible(Object object) {
        if (valueType == ValueType.BYTES) {
            return (object instanceof byte[]);
        } else if (valueType == ValueType.BOOLEAN) {
            return (object instanceof Boolean);
        } else if (valueType == ValueType.TEXT) {
            return (object instanceof String);
        } else if (valueType == ValueType.DATE || valueType == ValueType.DATE_TIME) {
            return (object instanceof DateValue || object instanceof DateTimeValue || object instanceof String);
        } else if (valueType == ValueType.TIME) {
            return (object instanceof TimeValue || object instanceof String);
        } else if (valueType == ValueType.NUMBER) {
            return (object instanceof Number || object instanceof String);
        } else if (valueType == ValueType.UUID) {
            return (object instanceof UUID || object instanceof String);
        } else if (valueType == ValueType.NOT_IDENTIFIED) {
            if (value == null) {
                return object == null;
            } else if (object != null) {
                return value.getClass().equals(object.getClass());
            }
        }

        return false;
    }
}
