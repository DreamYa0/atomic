package com.atomic.tools.db;

import com.atomic.tools.db.lettercase.LetterCase;
import com.atomic.tools.db.lettercase.WithColumnLetterCase;
import com.atomic.tools.db.lettercase.WithPrimaryKeyLetterCase;
import com.atomic.tools.db.lettercase.WithTableLetterCase;

import java.util.List;


public class Change implements DbElement, WithTableLetterCase, WithColumnLetterCase, WithPrimaryKeyLetterCase {

    /**
     * 日期的类型
     */
    private final DataType dataType;
    /**
     * 数据表名称
     */
    private final String dataName;
    /**
     * 主键名的集合
     */
    private final List<String> pksNameList;
    /**
     * 列名称的列表
     */
    private final List<String> columnsNameList;
    /**
     * 改变的类型
     */
    private final ChangeType changeType;
    /**
     * 行起始点
     */
    private final Row rowAtStartPoint;
    /**
     * 行起终点
     */
    private final Row rowAtEndPoint;

    private final LetterCase tableLetterCase;

    private final LetterCase columnLetterCase;

    private final LetterCase primaryKeyLetterCase;

    private Change(DataType dataType, String dataName, ChangeType changeType, Row rowAtStartPoint, Row rowAtEndPoint,
                   LetterCase tableLetterCase, LetterCase columnLetterCase, LetterCase primaryKeyLetterCase) {

        if (dataType == null) {
            throw new NullPointerException("The type of the data must be not null");
        }
        if (dataName == null) {
            throw new NullPointerException("The name of the data must be not null");
        }
        this.dataType = dataType;
        this.dataName = dataName;
        if (rowAtStartPoint != null) {
            this.pksNameList = rowAtStartPoint.getPksNameList();
            this.columnsNameList = rowAtStartPoint.getColumnsNameList();
        } else {
            this.pksNameList = rowAtEndPoint.getPksNameList();
            this.columnsNameList = rowAtEndPoint.getColumnsNameList();
        }
        this.changeType = changeType;
        this.rowAtStartPoint = rowAtStartPoint;
        this.rowAtEndPoint = rowAtEndPoint;
        this.tableLetterCase = tableLetterCase;
        this.columnLetterCase = columnLetterCase;
        this.primaryKeyLetterCase = primaryKeyLetterCase;
    }

    /**
     * 返回创建更改的新实例
     * @param dataType             数据类型变化
     * @param dataName             数据名称
     * @param rowAtEndPoint        行结束点
     * @param tableLetterCase      表的字母
     * @param columnLetterCase     列的字母
     * @param primaryKeyLetterCase 主键的字母大小写
     * @return 创建变更的新实例
     * @throws NullPointerException
     */
    static Change createCreationChange(DataType dataType, String dataName, Row rowAtEndPoint,
                                       LetterCase tableLetterCase, LetterCase columnLetterCase, LetterCase primaryKeyLetterCase) {
        return new Change(dataType, dataName, ChangeType.CREATION, null, rowAtEndPoint,
                tableLetterCase, columnLetterCase, primaryKeyLetterCase);
    }

    /**
     * 返回修改更改的新实例
     * @param dataType             数据类型变化
     * @param dataName             数据名称
     * @param rowAtStartPoint      行开始点
     * @param rowAtEndPoint        行结束点
     * @param tableLetterCase      表的字母
     * @param columnLetterCase     列的字母
     * @param primaryKeyLetterCase 主键的字母大小写
     * @return 创建变更的新实例
     * @throws NullPointerException
     */
    static Change createModificationChange(DataType dataType, String dataName, Row rowAtStartPoint, Row rowAtEndPoint,
                                           LetterCase tableLetterCase, LetterCase columnLetterCase, LetterCase primaryKeyLetterCase) {
        return new Change(dataType, dataName, ChangeType.MODIFICATION, rowAtStartPoint, rowAtEndPoint,
                tableLetterCase, columnLetterCase, primaryKeyLetterCase);
    }

    static Change createDeletionChange(DataType dataType, String dataName, Row rowAtStartPoint,
                                       LetterCase tableLetterCase, LetterCase columnLetterCase, LetterCase primaryKeyLetterCase) {
        return new Change(dataType, dataName, ChangeType.DELETION, rowAtStartPoint, null,
                tableLetterCase, columnLetterCase, primaryKeyLetterCase);
    }

    @Override
    public LetterCase getColumnLetterCase() {
        return columnLetterCase;
    }

    @Override
    public LetterCase getPrimaryKeyLetterCase() {
        return primaryKeyLetterCase;
    }

    @Override
    public LetterCase getTableLetterCase() {
        return tableLetterCase;
    }

    /**
     * 返回更改的数据类型
     * @return 变化的数据类型
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * 返回更改的数据的名称
     * @return 变化的数据名称
     */
    public String getDataName() {
        return dataName;
    }

    /**
     * 返回主键名称的列表
     * @return 主键名称的列表
     */
    public List<String> getPksNameList() {
        return pksNameList;
    }

    /**
     * 返回主键值的列表
     * @return 返回主键值的列表
     */
    public List<Value> getPksValueList() {
        if (rowAtStartPoint != null) {
            return rowAtStartPoint.getPksValueList();
        }
        return rowAtEndPoint.getPksValueList();
    }

    /**
     * 返回列名列表
     * @return 返回列名列表
     */
    public List<String> getColumnsNameList() {
        return columnsNameList;
    }

    /**
     * 返回更改的类型
     * @return 返回更改的类型
     */
    public ChangeType getChangeType() {
        return changeType;
    }

    /**
     * 返回起始点的行
     * @return 返回起始点的行
     */
    public Row getRowAtStartPoint() {
        return rowAtStartPoint;
    }

    /**
     * 返回终点处的行
     * @return 返回终点处的行
     */
    public Row getRowAtEndPoint() {
        return rowAtEndPoint;
    }
}
