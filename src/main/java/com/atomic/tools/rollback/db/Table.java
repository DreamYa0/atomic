package com.atomic.tools.rollback.db;

import com.atomic.exception.AssertJDBException;
import com.atomic.tools.rollback.db.lettercase.LetterCase;
import com.atomic.tools.rollback.db.util.NameComparator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Table extends AbstractDbData<Table> {

    /**
     * 表的名称
     */
    private String name;
    /**
     * 表的列列表
     */
    private List<String> columnsList;
    /**
     * 要检查的列
     */
    private String[] columnsToCheck;
    /**
     * 排除的列
     */
    private String[] columnsToExclude;
    /**
     * 列的顺序
     */
    private Order[] columnsToOrder;
    /**
     * 为列名称和表名开始分隔符
     */
    private Character startDelimiter = null;
    /**
     * 以列名称和表名结束分隔符
     */
    private Character endDelimiter = null;


    public Table() {
        super(Table.class, DataType.TABLE);
    }

    public Table(Source source, String name) {
        this(source, name, (String[]) null, (String[]) null);
    }

    /**
     * @param source
     * @param name             表的名称
     * @param columnsToCheck   要检查的列的名称数组
     * @param columnsToExclude 要排除的列的名称数组
     */
    public Table(Source source, String name, String[] columnsToCheck, String[] columnsToExclude) {
        this(source, name, null, columnsToCheck, columnsToExclude);
    }

    /**
     * @param dataSource
     * @param name       表的名称
     */
    public Table(DataSource dataSource, String name) {
        this(dataSource, name, (String[]) null, (String[]) null);
    }

    public Table(DataSource dataSource, String name, String[] columnsToCheck, String[] columnsToExclude) {
        this(dataSource, name, null, columnsToCheck, columnsToExclude);
    }

    public Table(Source source, String name, Order[] columnsToOrder) {
        this(source, name, columnsToOrder, null, null);
    }

    public Table(Source source, String name, Order[] columnsToOrder, String[] columnsToCheck, String[] columnsToExclude) {
        this(source, name, null, null, columnsToOrder, columnsToCheck, columnsToExclude);
    }

    public Table(DataSource dataSource, String name, Order[] columnsToOrder) {
        this(dataSource, name, columnsToOrder, null, null);
    }

    public Table(DataSource dataSource, String name, Order[] columnsToOrder, String[] columnsToCheck, String[] columnsToExclude) {
        this(dataSource, name, null, null, columnsToOrder, columnsToCheck, columnsToExclude);
    }

    public Table(Source source, String name, Character startDelimiter, Character endDelimiter) {
        this(source, name, startDelimiter, endDelimiter, null, null, null);
    }

    public Table(Source source, String name, Character startDelimiter, Character endDelimiter, String[] columnsToCheck, String[] columnsToExclude) {
        this(source, name, startDelimiter, endDelimiter, null, columnsToCheck, columnsToExclude);
    }

    public Table(DataSource dataSource, String name, Character startDelimiter, Character endDelimiter) {
        this(dataSource, name, startDelimiter, endDelimiter, null, null, null);
    }

    /**
     * 使用连接的构造函数，表的名称和列，以检查和排除
     * @param dataSource       数据库的数据源
     * @param name             表的名称
     * @param startDelimiter   为列名称和表名开始分隔符
     * @param endDelimiter     以列名称和表名结束分隔符
     * @param columnsToCheck   要检查的列的名称数组。如果{@code null}，这意味着检查所有的列
     * @param columnsToExclude 要排除的列的名称数组。如果{ @code 为null }，这意味着不排除列
     * @since 1.2.0
     */
    public Table(DataSource dataSource, String name, Character startDelimiter, Character endDelimiter, String[] columnsToCheck, String[] columnsToExclude) {
        this(dataSource, name, startDelimiter, endDelimiter, null, columnsToCheck, columnsToExclude);
    }

    /**
     * @param source
     * @param name           表的名称
     * @param startDelimiter 为列名称和表名开始分隔符
     * @param endDelimiter   以列名称和表名结束分隔符
     * @since 1.2.0
     */
    public Table(Source source, String name, Character startDelimiter, Character endDelimiter, Order[] columnsToOrder) {
        this(source, name, startDelimiter, endDelimiter, columnsToOrder, null, null);
    }

    /**
     * 表和列的名称用于检查和排除
     * @param source
     * @param name             表的名称
     * @param startDelimiter   为列名称和表名开始分隔符
     * @param endDelimiter     以列名称和表名结束分隔符
     * @param columnsToCheck   要检查的列的名称数组。如果{@code null}，这意味着检查所有的列
     * @param columnsToExclude 要排除的列的名称数组。如果{ @code 为null }，这意味着不排除列
     * @since 1.2.0
     */
    public Table(Source source, String name, Character startDelimiter, Character endDelimiter, Order[] columnsToOrder, String[] columnsToCheck, String[] columnsToExclude) {
        super(Table.class, DataType.TABLE, source);
        setName(name);
        setStartDelimiter(startDelimiter);
        setEndDelimiter(endDelimiter);
        setColumnsToOrder(columnsToOrder);
        setColumnsToCheck(columnsToCheck);
        setColumnsToExclude(columnsToExclude);
    }

    /**
     * 使用数据源和表名的构造函数
     * @param dataSource     数据库的数据源
     * @param name           表的名称
     * @param startDelimiter 为列名称和表名开始分隔符
     * @param endDelimiter   以列名称和表名结束分隔符
     * @since 1.2.0
     */
    public Table(DataSource dataSource, String name, Character startDelimiter, Character endDelimiter, Order[] columnsToOrder) {
        this(dataSource, name, startDelimiter, endDelimiter, columnsToOrder, null, null);
    }

    /**
     * 使用连接的构造函数，表的名称和列，以检查和排除
     * @param dataSource       数据库的数据源
     * @param name             表的名称
     * @param startDelimiter   为列名称和表名开始分隔符
     * @param endDelimiter     以列名称和表名结束分隔符
     * @param columnsToCheck   要检查的列的名称数组。如果{@code null}，这意味着检查所有的列
     * @param columnsToExclude 要排除的列的名称数组。如果{ @code 为null }，这意味着不排除列
     * @since 1.2.0
     */
    public Table(DataSource dataSource, String name, Character startDelimiter, Character endDelimiter, Order[] columnsToOrder, String[] columnsToCheck, String[] columnsToExclude) {
        super(Table.class, DataType.TABLE, dataSource);
        setName(name);
        setStartDelimiter(startDelimiter);
        setEndDelimiter(endDelimiter);
        setColumnsToOrder(columnsToOrder);
        setColumnsToCheck(columnsToCheck);
        setColumnsToExclude(columnsToExclude);
    }

    public String getName() {
        return name;
    }

    public Table setName(String name) {
        if (name == null) {
            throw new NullPointerException("name can not be null");
        }
        this.name = name;
        setNameFromDb();
        return this;
    }

    @Override
    public Table setDataSource(DataSource dataSource) {
        Table table = super.setDataSource(dataSource);
        setNameFromDb();
        return table;
    }

    @Override
    public Table setSource(Source source) {
        Table table = super.setSource(source);
        setNameFromDb();
        return table;
    }

    /**
     * 从数据库中对应的名称中设置名称
     */
    private void setNameFromDb() {
        if (name != null && (getSource() != null || getDataSource() != null)) {
            try (Connection connection = getConnection()) {
                LetterCase tableLetterCase = getTableLetterCase();
                LetterCase columnLetterCase = getColumnLetterCase();

                DatabaseMetaData metaData = connection.getMetaData();
                try (ResultSet tableResultSet = metaData.getTables(getCatalog(connection), getSchema(connection), null,
                        new String[]{"TABLE"})) {
                    while (tableResultSet.next()) {
                        String tableName = tableResultSet.getString("TABLE_NAME");
                        if (tableLetterCase.isEqual(tableName, name)) {
                            name = tableLetterCase.convert(tableName);
                            break;
                        }
                    }
                }

                columnsList = new ArrayList<>();
                try (ResultSet columnsResultSet = metaData.getColumns(getCatalog(connection), getSchema(connection), name, null)) {
                    while (columnsResultSet.next()) {
                        String column = columnsResultSet.getString("COLUMN_NAME");
                        columnsList.add(columnLetterCase.convert(column));
                    }
                }
            } catch (SQLException e) {
                throw new AssertJDBException(e);
            }
        }
    }

    /**
     * 返回要检查的列
     * @return 返回要检查的列
     */
    public String[] getColumnsToCheck() {
        if (columnsToCheck == null) {
            return null;
        }
        return columnsToCheck.clone();
    }

    /**
     * 设置要检查的列
     * @param columnsToCheck 要检查的列的名称数组。如果{@code null}，这意味着检查所有的列
     * @return 实例
     * @throws NullPointerException
     */
    public Table setColumnsToCheck(String[] columnsToCheck) {
        if (columnsList == null) {
            throw new AssertJDBException("The table name and the source or datasource must be set first");
        }
        if (columnsToCheck != null) {
            LetterCase letterCase = getColumnLetterCase();
            // If the parameter is not null, all the names are convert
            // before setting the instance field
            List<String> columnsToCheckList = new ArrayList<String>();
            handleColumsList(columnsToCheckList, columnsToCheck, columnsList, letterCase);
            this.columnsToCheck = columnsToCheckList.toArray(new String[0]);
        } else {
            this.columnsToCheck = null;
        }
        return this;
    }

    private void handleColumsList(List<String> columnsToCheckList, String[] columnsToCheck, List<String> columnsList, LetterCase letterCase) {
        for (int index = 0; index < columnsToCheck.length; index++) {
            String column = columnsToCheck[index];
            if (column == null) {
                throw new NullPointerException("The name of the column can not be null");
            }
            int indexOf = NameComparator.INSTANCE.indexOf(columnsList, column, letterCase);
            if (indexOf != -1) {
                columnsToCheckList.add(columnsList.get(indexOf));
            }
        }
    }

    /**
     * 返回要排除的列
     * @return 返回要排除的列
     */
    public String[] getColumnsToExclude() {
        if (columnsToExclude == null) {
            return null;
        }
        return columnsToExclude.clone();
    }

    /**
     * 设置要排除的列
     * @param columnsToExclude 列
     */
    public Table setColumnsToExclude(String[] columnsToExclude) {
        if (columnsList == null) {
            throw new AssertJDBException("The table name and the source or datasource must be set first");
        }
        if (columnsToExclude != null) {
            LetterCase letterCase = getColumnLetterCase();
            this.columnsToExclude = new String[columnsToExclude.length];
            List<String> columnsToExcludeList = new ArrayList<String>();
            handleColumsList(columnsToExcludeList, columnsToExclude, columnsList, letterCase);
            this.columnsToExclude = columnsToExcludeList.toArray(new String[0]);
        } else {
            this.columnsToExclude = null;
        }
        return this;
    }

    public Order[] getColumnsToOrder() {
        if (columnsToOrder == null) {
            return null;
        }
        return columnsToOrder.clone();
    }

    public Table setColumnsToOrder(Order[] columnsToOrder) {
        if (columnsList == null) {
            throw new AssertJDBException("The table name and the source or datasource must be set first");
        }
        if (columnsToOrder != null) {
            LetterCase letterCase = getColumnLetterCase();
            this.columnsToOrder = new Order[columnsToOrder.length];
            List<Order> columnsToOrderList = new ArrayList<Order>();
            for (int index = 0; index < columnsToOrder.length; index++) {
                Order order = columnsToOrder[index];
                if (order == null) {
                    throw new NullPointerException("The order can not be null");
                }
                String column = order.getName();
                if (column == null) {
                    throw new NullPointerException("The name of the column for order can not be null");
                }
                int indexOf = NameComparator.INSTANCE.indexOf(columnsList, column, letterCase);
                if (indexOf != -1) {
                    String columnName = columnsList.get(indexOf);
                    columnsToOrderList.add(Order.getOrder(columnName, order.getType()));
                }
            }
            this.columnsToOrder = columnsToOrderList.toArray(new Order[0]);
        } else {
            this.columnsToOrder = null;
        }
        return this;
    }

    /**
     * 返回列名称和表名的开始分隔符
     * @return 返回列名称和表名的开始分隔符
     * @since 1.2.0
     */
    public Character getStartDelimiter() {
        return startDelimiter;
    }

    /**
     * 设置列名称和表名的开始分隔符
     * @param startDelimiter
     * @since 1.2.0
     */
    public Table setStartDelimiter(Character startDelimiter) {
        this.startDelimiter = startDelimiter;
        return this;
    }

    /**
     * 返回列名称和表名的结束分隔符
     * @return 返回列名称和表名的结束分隔符
     * @since 1.2.0
     */
    public Character getEndDelimiter() {
        return endDelimiter;
    }

    public Table setEndDelimiter(Character endDelimiter) {
        this.endDelimiter = endDelimiter;
        return this;
    }

    /**
     * 编码列名称和表名
     * @param name 列名或表名
     */
    private String encode(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        if (startDelimiter != null) {
            stringBuilder.append(startDelimiter);
        }
        stringBuilder.append(name);
        if (endDelimiter != null) {
            stringBuilder.append(endDelimiter);
        }
        return stringBuilder.toString();
    }

    /**
     * 返回SQL
     * @return 返回SQL
     * @throws NullPointerException
     */
    public String getRequest() {
        if (name == null) {
            throw new NullPointerException("name can not be null");
        }

        // Get the request about the name of the table and the columns to check
        StringBuilder stringBuilder = new StringBuilder("SELECT ");
        if (columnsToCheck == null) {
            stringBuilder.append("*");
        } else {
            for (String column : columnsToCheck) {
                if (stringBuilder.length() > 7) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(encode(column));
            }
        }
        stringBuilder.append(" FROM ");
        stringBuilder.append(encode(name));
        if (columnsToOrder != null) {
            for (int index = 0; index < columnsToOrder.length; index++) {
                if (index == 0) {
                    stringBuilder.append(" ORDER BY ");
                } else {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(encode(columnsToOrder[index].getName()));
                if (columnsToOrder[index].getType() == Order.OrderType.DESC) {
                    stringBuilder.append(" DESC");
                    stringBuilder.append(" LIMIT 0,1000");
                }
            }
        } else {
            stringBuilder.append(" ORDER BY ");
            stringBuilder.append(encode(columnsList.get(0)));
            stringBuilder.append(" DESC");
            stringBuilder.append(" LIMIT 0,1000");
        }
        return stringBuilder.toString();
    }

    private void collectColumnsNameFromResultSet(ResultSet resultSet) throws SQLException {
        LetterCase letterCase = getColumnLetterCase();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        List<String> columnsNameList = new ArrayList<>();
        List<String> columnsToExcludeList = null;
        if (columnsToExclude != null) {
            columnsToExcludeList = Arrays.asList(columnsToExclude);
        }

        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            String columnName = letterCase.convert(resultSetMetaData.getColumnLabel(i));
            if (columnsToExcludeList == null
                    || !NameComparator.INSTANCE.contains(columnsToExcludeList, columnName, letterCase)) {

                columnsNameList.add(columnName);
            }
        }
        setColumnsNameList(columnsNameList);
    }

    private void collectPrimaryKeyName(Connection connection) throws SQLException {
        String catalog = getCatalog(connection);
        String schema = getSchema(connection);
        List<String> pksNameList = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        String tableName = name;
        try (ResultSet resultSet = metaData.getTables(catalog, schema, null, new String[]{"TABLE"})) {
            LetterCase letterCase = getTableLetterCase();
            while (resultSet.next()) {
                String tableResult = resultSet.getString("TABLE_NAME");
                if (letterCase.isEqual(tableName, tableResult)) {
                    tableName = tableResult;
                    break;
                }
            }
        }

        try (ResultSet resultSet = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            LetterCase letterCase = getPrimaryKeyLetterCase();
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                if (NameComparator.INSTANCE.contains(getColumnsNameList(), columnName, letterCase)) {
                    String pkName = letterCase.convert(columnName);
                    pksNameList.add(pkName);
                }
            }
        }
        setPksNameList(pksNameList);
    }

    @Override
    protected void loadImpl(Connection connection) throws SQLException {
        if (name == null) {
            throw new NullPointerException("name can not be null");
        }

        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(getRequest())) {
                collectColumnsNameFromResultSet(resultSet);
                collectRowsFromResultSet(resultSet);
            }
        }
        collectPrimaryKeyName(connection);
        if (columnsToOrder == null) {
            sortRows();
        }
    }

    public static class Order {
        /**
         * 订单的名称
         */
        private String name;
        /**
         * 顺序的类型
         */
        private OrderType type;

        private Order(String name, OrderType type) {
            this.name = name;
            this.type = type;
        }

        /**
         * 构建升序
         * @param name
         * @return 一个升序排序
         */
        public static Order asc(String name) {
            return getOrder(name, OrderType.ASC);
        }

        /**
         * 构建降序
         * @param name
         * @return 降序排列
         */
        public static Order desc(String name) {
            return getOrder(name, OrderType.DESC);
        }

        private static Order getOrder(String name, OrderType type) {
            return new Order(name, type);
        }

        public String getName() {
            return name;
        }

        public OrderType getType() {
            return type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Order) {
                Order order = (Order) obj;
                if (order.type == type) {
                    if ((name == null && order.name == null) ||
                            (name != null && name.equals(order.name))) {

                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 枚举类型的顺序
         */
        public enum OrderType {
            /**
             * 升序排序
             */
            ASC,
            /**
             * 降序排列
             */
            DESC;
        }
    }
}
