package com.atomic.tools.db;


import com.atomic.exception.AssertJDBException;
import com.atomic.tools.db.lettercase.LetterCase;
import com.atomic.tools.db.util.NameComparator;
import com.atomic.tools.db.util.RowComparator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 这个类表示来自数据库的数据
 * @version 1.0.0
 */
public abstract class AbstractDbData<D extends AbstractDbData<D>> extends AbstractDbElement<D> {

    /**
     * 数据类型
     */
    private final DataType dataType;
    /**
     * 列名列表
     */
    private List<String> columnsNameList;
    /**
     * 主键的名称列表。
     */
    private List<String> pksNameList;
    /**
     * 行列表
     */
    private List<Row> rowsList;
    /**
     * 列列表
     */
    private List<Column> columnsList;

    /**
     * @param dataType 数据的类型.
     * @param selfType
     */
    AbstractDbData(Class<D> selfType, DataType dataType) {
        super(selfType);
        this.dataType = dataType;
    }

    AbstractDbData(Class<D> selfType, DataType dataType, Source source) {
        super(selfType, source);
        this.dataType = dataType;
    }

    AbstractDbData(Class<D> selfType, DataType dataType, DataSource dataSource) {
        super(selfType, dataSource);
        this.dataType = dataType;
    }

    public DataType getDataType() {
        return dataType;
    }

    /**
     * 获取SQL
     */
    public abstract String getRequest();

    /**
     * 从数据库中加载数据的信息。
     * @throws NullPointerException
     * @throws AssertJDBException
     */
    private void load() {
        try (Connection connection = getConnection()) {
            // 根据表或请求调用特定的加载
            loadImpl(connection);
            if (pksNameList == null) {
                pksNameList = new ArrayList<>();
            }
        } catch (SQLException e) {
            throw new AssertJDBException(e);
        }
    }

    /**
     * 排序的列表的行
     */
    protected void sortRows() {
        Collections.sort(rowsList, RowComparator.INSTANCE);
    }

    /**
     * 加载的实现取决于类型的数据。
     * @param connection 数据库连接
     * @throws SQLException SQL Exception.
     * @see Table           #loadImpl(Connection)
     * @see Request         #loadImpl(Connection)
     */
    protected abstract void loadImpl(Connection connection) throws SQLException;

    /**
     * 从结果集中收集行
     * @param resultSet 结果集
     * @throws SQLException
     */
    protected void collectRowsFromResultSet(ResultSet resultSet) throws SQLException {
        // TODO 需要优化提升处理效率
        ResultSetMetaData metaData = resultSet.getMetaData();
        rowsList = new ArrayList<>();
        while (resultSet.next()) {
            List<Value> valuesList = new ArrayList<>();
            for (String columnName : columnsNameList) {
                // TODO 改进类型的检查
                int index = -1;
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if (getColumnLetterCase().isEqual(columnName, metaData.getColumnLabel(i))) {
                        index = i;
                        break;
                    }
                }
                Object object;
                int type = metaData.getColumnType(index);
                switch (type) {
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.CHAR:
                        object = resultSet.getString(columnName);
                        break;

                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        object = resultSet.getInt(columnName);
                        break;

                    case Types.BIGINT:
                        object = resultSet.getLong(columnName);
                        break;

                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        object = resultSet.getBigDecimal(columnName);
                        break;

                    case Types.DOUBLE:
                        object = resultSet.getDouble(columnName);
                        break;

                    case Types.REAL:
                    case Types.FLOAT:
                        object = resultSet.getFloat(columnName);
                        break;

                    case Types.BINARY:
                    case Types.VARBINARY:
                        object = resultSet.getByte(columnName);
                        break;

                    case Types.BOOLEAN:
                        object = resultSet.getBoolean(columnName);
                        break;
                    case Types.DATE:
                        object = resultSet.getDate(columnName);
                        break;
                    case Types.TIME:
                        object = resultSet.getTime(columnName);
                        break;
                    case Types.TIMESTAMP:
                        object = resultSet.getTimestamp(columnName);
                        break;
                    case Types.BLOB:
                        object = resultSet.getBytes(columnName);
                        break;
                    case Types.CLOB:
                        object = resultSet.getString(columnName);
                        break;
                    case Types.BIT:
                        object = resultSet.getInt(columnName);
                        break;
                    default:
                        object = resultSet.getObject(columnName);
                        break;
                }
                valuesList.add(new Value(columnName, object, getColumnLetterCase()));
            }
            rowsList.add(new Row(pksNameList, columnsNameList, valuesList, getColumnLetterCase(), getPrimaryKeyLetterCase()));
        }
    }

    /**
     * 返回的列表的列的名称来自数据库的数据。
     * @return 表的列名集合
     */
    public List<String> getColumnsNameList() {
        if (columnsNameList == null) {
            load();
        }
        return columnsNameList;
    }

    /**
     * 列表名称集合
     * @param columnsNameList 表的列名集合
     */
    protected void setColumnsNameList(List<String> columnsNameList) {
        this.columnsNameList = columnsNameList;
    }

    /**
     * 返回主键的列表的名称来自数据库的数据。
     * @return
     */
    public List<String> getPksNameList() {
        if (pksNameList == null) {
            load();
        }
        return pksNameList;
    }

    /**
     * 设置主键名称的列表
     * @param pksNameList 主键名称的列表
     * @throws AssertJDBException 如果在列名称中不存在主键，则触发异常
     */
    protected void setPksNameList(List<String> pksNameList) {
        this.pksNameList = new ArrayList<>();
        this.pksNameList.addAll(pksNameList);
        if (rowsList != null) {
            for (Row row : rowsList) {
                row.setPksNameList(this.pksNameList);
            }
        }
        controlIfAllThePksNameExistInTheColumns();
    }

    /**
     * 控制所有的主键存在于列名称
     */
    protected void controlIfAllThePksNameExistInTheColumns() {
        LetterCase letterCase = getPrimaryKeyLetterCase();
        if (pksNameList != null) {
            for (String pkName : pksNameList) {
                // 如果不设置列名称列表，则不测试列的存在
                if (columnsNameList != null) {
                    if (!NameComparator.INSTANCE.contains(columnsNameList, pkName, letterCase)) {
                        throw new AssertJDBException("Primary key %s do not exist in the columns %s", pkName, columnsNameList);
                    }
                }
            }
        }
    }

    /**
     * 返回数据库中数据行的值列表
     * <p>
     * 如果它是第一个调用 {@code getRowsList()}, 数据通过调用{@link #load()}私有方法从数据库加载
     * </p>
     * @return 值的列表
     * @throws NullPointerException
     * @throws AssertJDBException
     */
    public List<Row> getRowsList() {
        if (rowsList == null) {
            load();
        }
        return rowsList;
    }

    /**
     * 返回数据库中数据列中的值列表
     * @return 列中的值列表
     * @throws NullPointerException
     * @throws AssertJDBException
     */
    public List<Column> getColumnsList() {
        if (columnsList == null) {
            columnsList = new ArrayList<>();
            List<String> columnsNameList = getColumnsNameList();
            int index = 0;
            for (String name : columnsNameList) {
                List<Value> valuesList = getValuesList(index);
                Column column = new Column(name, valuesList, getColumnLetterCase());
                columnsList.add(column);
                index++;
            }
        }
        return columnsList;
    }

    /**
     * 返回对应于参数中的列索引和列内值的列
     * @param index 列索引
     * @return 列和值
     */
    public Column getColumn(int index) {
        return getColumnsList().get(index);
    }

    /**
     * 返回对应于索引的行
     * @param index 指标
     * @return The {@link Row}
     */
    public Row getRow(int index) {
        return getRowsList().get(index);
    }

    /**
     * 返回对应于列名的列的值
     * @param index 列索引
     * @return 值
     */
    private List<Value> getValuesList(int index) {
        return getRowsList().stream().map(row -> row.getColumnValue(index)).collect(Collectors.toList());
    }

    /**
     * @param pksValues 主键值
     * @return 行
     */
    public Row getRowFromPksValues(Value... pksValues) {
        for (Row row : getRowsList()) {
            if (row.hasPksValuesEqualTo(pksValues)) {
                return row;
            }
        }
        return null;
    }
}
