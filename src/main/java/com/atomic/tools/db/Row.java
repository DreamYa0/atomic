package com.atomic.tools.db;


import com.atomic.tools.db.lettercase.LetterCase;
import com.atomic.tools.db.lettercase.WithColumnLetterCase;
import com.atomic.tools.db.lettercase.WithPrimaryKeyLetterCase;
import com.atomic.tools.db.util.NameComparator;
import com.atomic.tools.db.util.Values;

import java.util.ArrayList;
import java.util.List;

public class Row implements DbElement, WithColumnLetterCase, WithPrimaryKeyLetterCase {

    /**
     * 列名称的列表
     */
    private final List<String> columnsNameList;
    private final List<Value> valuesList;
    private final LetterCase columnLetterCase;
    private final LetterCase primaryKeyLetterCase;
    /**
     * 主键名的列表
     */
    private List<String> pksNameList;

    /**
     * @param pksNameList          主键名的列表
     * @param columnsNameList      列名称的列表
     * @param valuesList           值
     * @param columnLetterCase     列的字母大小写
     * @param primaryKeyLetterCase 主键的字母大小写
     */
    Row(List<String> pksNameList, List<String> columnsNameList, List<Value> valuesList,
        LetterCase columnLetterCase, LetterCase primaryKeyLetterCase) {

        this.pksNameList = pksNameList;
        this.columnsNameList = columnsNameList;
        this.valuesList = valuesList;
        this.columnLetterCase = columnLetterCase;
        this.primaryKeyLetterCase = primaryKeyLetterCase;
    }

    @Override
    public LetterCase getColumnLetterCase() {
        return columnLetterCase;
    }

    @Override
    public LetterCase getPrimaryKeyLetterCase() {
        return primaryKeyLetterCase;
    }

    public List<String> getPksNameList() {
        return pksNameList;
    }

    void setPksNameList(List<String> pksNameList) {
        this.pksNameList = pksNameList;
    }

    public List<Value> getPksValueList() {
        List<Value> pksValueList = new ArrayList<>();
        handleValuesList(pksValueList, pksNameList);
        return pksValueList;
    }

    public List<String> getColumnsNameList() {
        return columnsNameList;
    }

    public List<Value> getValuesList() {
        return valuesList;
    }

    public Value[] getPksValues() {
        List<Value> pksValuesList = new ArrayList<>();
        handleValuesList(pksValuesList, pksNameList);
        return pksValuesList.toArray(new Value[pksValuesList.size()]);
    }

    private void handleValuesList(List<Value> pksValuesList, List<String> pksNameList) {
        if (pksNameList != null) {
            for (String pkName : pksNameList) {
                int index = NameComparator.INSTANCE.indexOf(columnsNameList, pkName, primaryKeyLetterCase);
                Value value = valuesList.get(index);
                pksValuesList.add(value);
            }
        }
    }

    /**
     * 如果主键的值与参数中的值相等，则返回 true
     * @param pksValues 主键的值比较
     * @return
     */
    public boolean hasPksValuesEqualTo(Value[] pksValues) {
        Value[] pksValues1 = getPksValues();
        if (pksValues1.length != 0 && pksValues1.length == pksValues.length) {
            for (int index = 0; index < pksValues1.length; index++) {
                if (!Values.areEqual(pksValues1[index], pksValues[index].getValue())) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    public boolean hasValues(Row row) {
        List<Value> valuesList = getValuesList();
        List<Value> rowValuesList = row.getValuesList();
        for (int index = 0; index < valuesList.size(); index++) {
            Value value = valuesList.get(index);
            Value rowValue = rowValuesList.get(index);
            if (!Values.areEqual(value, rowValue.getValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 返回对应于列索引的值
     * @param index
     * @return
     */
    public Value getColumnValue(int index) {
        return valuesList.get(index);
    }

    public Value getColumnValue(String columnName) {
        if (columnName == null) {
            throw new NullPointerException("Column name must be not null");
        }

        int index = NameComparator.INSTANCE.indexOf(columnsNameList, columnName, columnLetterCase);
        if (index == -1) {
            return null;
        }
        return getColumnValue(index);
    }
}
