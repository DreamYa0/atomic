package com.atomic.tools.rollback.db;


import com.atomic.tools.rollback.db.lettercase.LetterCase;
import com.atomic.tools.rollback.db.lettercase.WithColumnLetterCase;

import java.util.List;

public class Column implements DbElement, WithColumnLetterCase {

    /**
     * 列的名称
     */
    private final String name;
    /**
     * 列的值
     */
    private final List<Value> valuesList;

    private final LetterCase columnLetterCase;

    Column(String name, List<Value> valuesList, LetterCase columnLetterCase) {
        this.name = name;
        this.valuesList = valuesList;
        this.columnLetterCase = columnLetterCase;
    }

    @Override
    public LetterCase getColumnLetterCase() {
        return columnLetterCase;
    }

    public String getName() {
        return name;
    }

    public List<Value> getValuesList() {
        return valuesList;
    }

    /**
     * 返回对应于行索引的值
     * @param index 索引
     * @return 值
     */
    public Value getRowValue(int index) {
        return valuesList.get(index);
    }
}
