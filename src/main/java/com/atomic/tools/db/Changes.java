package com.atomic.tools.db;


import com.atomic.exception.AssertJDBException;
import com.atomic.tools.db.util.ChangeComparator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 数据库的变更
 */
public class Changes extends AbstractDbElement<Changes> {

    /**
     * 表集合
     */
    private List<Table> tablesList;
    /**
     * 开始点的表的列表
     */
    private List<Table> tablesAtStartPointList;
    /**
     * 结束点的表的列表
     */
    private List<Table> tablesAtEndPointList;

    private Request request;
    /**
     * 开始点的请求
     */
    private Request requestAtStartPoint;
    /**
     * 结束点的请求
     */
    private Request requestAtEndPoint;
    /**
     * 变更集合
     */
    private List<Change> changesList;

    public Changes() {
        super(Changes.class);
    }

    public Changes(Source source) {
        super(Changes.class, source);
    }

    public Changes(DataSource dataSource) {
        super(Changes.class, dataSource);
    }

    public Changes(Table... tables) {
        super(Changes.class);
        setTables(tables);
    }

    public Changes(Request request) {
        super(Changes.class);
        setRequest(request);
    }

    private static void copyElement(AbstractDbElement<?> elementToCopy, AbstractDbElement<?> element) {
        if (elementToCopy.getSource() != null) {
            element.setSource(elementToCopy.getSource());
        }
        if (elementToCopy.getDataSource() != null) {
            element.setDataSource(elementToCopy.getDataSource());
        }
    }

    private static Request getDuplicatedRequest(Request request) {
        Request r = new Request();
        copyElement(request, r);
        return r.setLetterCases(request.getTableLetterCase(),
                request.getColumnLetterCase(),
                request.getPrimaryKeyLetterCase())
                .setRequest(request.getRequest())
                .setParameters(request.getParameters())
                .setPksName(request.getPksNameList().toArray(new String[request.getPksNameList().size()]));
    }

    private static Table getDuplicatedTable(Table table) {
        Table t = new Table();
        copyElement(table, t);
        return t.setLetterCases(table.getTableLetterCase(),
                table.getColumnLetterCase(),
                table.getPrimaryKeyLetterCase())
                .setName(table.getName())
                .setStartDelimiter(table.getStartDelimiter())
                .setEndDelimiter(table.getEndDelimiter())
                .setColumnsToCheck(table.getColumnsToCheck())
                .setColumnsToExclude(table.getColumnsToExclude())
                .setColumnsToOrder(table.getColumnsToOrder());
    }

    /**
     * 添加表
     * @param tables 表对象集合
     * @return 变更集合
     */
    public Changes setTables(Table... tables) {
        request = null;
        requestAtStartPoint = null;
        requestAtEndPoint = null;
        tablesList = new ArrayList<>();
        tablesAtStartPointList = null;
        tablesAtEndPointList = null;
        changesList = null;
        for (Table table : tables) {
            if (table == null) {
                throw new NullPointerException("---------------Table 对象不能为空！---------------");
            }
            Table t = getDuplicatedTable(table);
            tablesList.add(t);
        }
        if (tables.length > 0) {
            copyElement(tables[0], this);
        }
        return myself;
    }

    /**
     * 获取表集合
     * @return
     */
    public List<Table> getTablesList() {
        return tablesList;
    }

    public Request getRequest() {
        return request;
    }

    public Changes setRequest(Request request) {
        if (request == null) {
            throw new NullPointerException("---------------Request 对象不能为空！---------------");
        }
        tablesList = null;
        tablesAtStartPointList = null;
        tablesAtEndPointList = null;
        this.request = getDuplicatedRequest(request);
        copyElement(request, this);
        requestAtStartPoint = null;
        requestAtEndPoint = null;
        changesList = null;
        return myself;
    }

    public List<Table> getTablesAtStartPointList() {
        return tablesAtStartPointList;
    }

    public List<Table> getTablesAtEndPointList() {
        return tablesAtEndPointList;
    }

    public Request getRequestAtStartPoint() {
        return requestAtStartPoint;
    }

    public Request getRequestAtEndPoint() {
        return requestAtEndPoint;
    }

    /**
     * 设置监听开始点
     * @return 变更集合
     */
    public Changes setStartPointNow() {
        if (request == null && tablesList == null) {
            try (Connection connection = getConnection()) {
                tablesList = new LinkedList<>();
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet resultSet = metaData.getTables(getCatalog(connection), getSchema(connection), null,
                        new String[]{"TABLE"});
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    Table t = new Table().setLetterCases(getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase())
                            .setName(getTableLetterCase().convert(tableName));
                    copyElement(this, t);
                    tablesList.add(t);
                }
            } catch (SQLException e) {
                throw new AssertJDBException(e);
            }
        }

        if (request != null) {
            tablesAtStartPointList = null;
            requestAtStartPoint = getDuplicatedRequest(request);
            requestAtStartPoint.getRowsList();
        } else {
            requestAtStartPoint = null;
            tablesAtStartPointList = new LinkedList<>();
            for (Table table : tablesList) {
                Table t = getDuplicatedTable(table);
                t.getRowsList();
                tablesAtStartPointList.add(t);
            }
        }
        tablesAtEndPointList = null;
        requestAtEndPoint = null;
        changesList = null;

        return myself;
    }

    /**
     * 设置监听结束点
     * @return 变更集合
     * @throws AssertJDBException JDBC断言异常
     */
    public Changes setEndPointNow() {
        if (requestAtStartPoint == null && tablesAtStartPointList == null) {
            throw new AssertJDBException("设置结束监听时未开启监听！");
        }

        if (requestAtStartPoint != null) {
            requestAtEndPoint = getDuplicatedRequest(request);
            requestAtEndPoint.getRowsList();
        } else {
            tablesAtEndPointList = new LinkedList<>();
            for (Table table : tablesList) {
                Table t = getDuplicatedTable(table);
                t.getRowsList();
                tablesAtEndPointList.add(t);
            }
        }
        changesList = null;

        return myself;
    }

    /**
     * 当有主键时，返回数据的更改列表
     * @param dataName         表名
     * @param dataAtStartPoint 数据开始点
     * @param dataAtEndPoint   数据结束点
     * @return 数据更改列表
     */
    private List<Change> getChangesListWithPks(String dataName, AbstractDbData<?> dataAtStartPoint,
                                               AbstractDbData<?> dataAtEndPoint) {

        List<Change> changesList = new ArrayList<>();

        // 列出所创建的行 : 该行不存在于起始点
        /*for (Row row : dataAtEndPoint.getRowsList()) {
            Row rowAtStartPoint = dataAtStartPoint.getRowFromPksValues(row.getPksValues());
            if (rowAtStartPoint == null) {
                Change change = createCreationChange(dataAtEndPoint.getDataType(), dataName, row,
                        getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase());
                changesList.add(change);
            }
        }*/

        // 并行处理提高执行效率
        dataAtEndPoint.getRowsList().parallelStream().forEach(row -> {
            Row rowAtStartPoint = dataAtStartPoint.getRowFromPksValues(row.getPksValues());
            if (rowAtStartPoint == null) {
                Change change = Change.createCreationChange(dataAtEndPoint.getDataType(), dataName, row,
                        getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase());
                changesList.add(change);
            }
        });

        /*for (Row row : dataAtStartPoint.getRowsList()) {
            Row rowAtEndPoint = dataAtEndPoint.getRowFromPksValues(row.getPksValues());
            if (rowAtEndPoint == null) {
                // 列出已删除的行 : 该行不存在于端点
                Change change = createDeletionChange(dataAtStartPoint.getDataType(), dataName, row,
                        getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase());
                changesList.add(change);
            } else {
                // 列表修改的行
                if (!row.hasValues(rowAtEndPoint)) {
                    // 如果行中至少有一个值是不同的，请添加更改
                    Change change = createModificationChange(dataAtStartPoint.getDataType(), dataName, row, rowAtEndPoint,
                            getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase());
                    changesList.add(change);
                }
            }
        }*/

        // 并行处理提高执行效率
        dataAtStartPoint.getRowsList().parallelStream().forEach(row -> {
            Row rowAtEndPoint = dataAtEndPoint.getRowFromPksValues(row.getPksValues());
            if (rowAtEndPoint == null) {
                // 列出已删除的行 : 该行不存在于端点
                Change change = Change.createDeletionChange(dataAtStartPoint.getDataType(), dataName, row,
                        getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase());
                changesList.add(change);
            } else {
                // 列表修改的行
                if (!row.hasValues(rowAtEndPoint)) {
                    // 如果行中至少有一个值是不同的，请添加更改
                    Change change = Change.createModificationChange(dataAtStartPoint.getDataType(), dataName, row, rowAtEndPoint,
                            getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase());
                    changesList.add(change);
                }
            }
        });
        return changesList;
    }

    /**
     * 当没有主键时，返回数据的更改列表
     * @param dataName         表名
     * @param dataAtStartPoint 数据开始点
     * @param dataAtEndPoint   数据结束点
     * @return 数据更改列表
     */
    private List<Change> getChangesListWithoutPks(String dataName, AbstractDbData<?> dataAtStartPoint,
                                                  AbstractDbData<?> dataAtEndPoint) {

        List<Change> changesList = new ArrayList<>();

        // 列出所创建的行:该行不存在于起始点
        List<Row> rowsAtStartPointList = new ArrayList<>(dataAtStartPoint.getRowsList());
        for (Row rowAtEndPoint : dataAtEndPoint.getRowsList()) {
            int index = -1;
            int index1 = 0;
            for (Row rowAtStartPoint : rowsAtStartPointList) {
                if (rowAtEndPoint.hasValues(rowAtStartPoint)) {
                    index = index1;
                    break;
                }
                index1++;
            }
            if (index == -1) {
                Change change = Change.createCreationChange(dataAtStartPoint.getDataType(), dataName, rowAtEndPoint,
                        getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase());
                changesList.add(change);
            } else {
                rowsAtStartPointList.remove(index);
            }
        }
        // 列出已删除的行:该行不存在于端点
        List<Row> rowsAtEndPointList = new ArrayList<>(dataAtEndPoint.getRowsList());
        for (Row rowAtStartPoint : dataAtStartPoint.getRowsList()) {
            int index = -1;
            int index1 = 0;
            for (Row rowAtEndPoint : rowsAtEndPointList) {
                if (rowAtStartPoint.hasValues(rowAtEndPoint)) {
                    index = index1;
                    break;
                }
                index1++;
            }
            if (index == -1) {
                Change change = Change.createDeletionChange(dataAtStartPoint.getDataType(), dataName, rowAtStartPoint,
                        getTableLetterCase(), getColumnLetterCase(), getPrimaryKeyLetterCase());
                changesList.add(change);
            } else {
                rowsAtEndPointList.remove(index);
            }
        }

        return changesList;
    }

    private List<Change> getChangesList(String dataName, AbstractDbData<?> dataAtStartPoint,
                                        AbstractDbData<?> dataAtEndPoint) {

        if (dataAtStartPoint.getPksNameList().size() > 0) {
            return getChangesListWithPks(dataName, dataAtStartPoint, dataAtEndPoint);
        } else {
            return getChangesListWithoutPks(dataName, dataAtStartPoint, dataAtEndPoint);
        }
    }

    /**
     * 返回更改的列表
     * @return 返回更改的列表
     * @throws AssertJDBException JDBC断言异常
     */
    public List<Change> getChangesList() {
        if (changesList == null) {
            if (requestAtEndPoint == null && tablesAtEndPointList == null) {
                throw new AssertJDBException("获取数据库变更信息前需先结束监听！");
            }

            if (requestAtEndPoint != null) {
                changesList = getChangesList(requestAtStartPoint.getRequest(), requestAtStartPoint, requestAtEndPoint);
            } else {
                changesList = new ArrayList<>();
                Iterator<Table> iteratorAtStartPoint = tablesAtStartPointList.iterator();
                Iterator<Table> iteratorAtEndPoint = tablesAtEndPointList.iterator();
                while (iteratorAtStartPoint.hasNext()) {
                    Table tableAtStartPoint = iteratorAtStartPoint.next();
                    Table tableAtEndPoint = iteratorAtEndPoint.next();
                    changesList.addAll(getChangesList(tableAtStartPoint.getName(), tableAtStartPoint, tableAtEndPoint));
                }
            }
        }

        changesList.sort(ChangeComparator.INSTANCE);
        return changesList;
    }

    /**
     * 根据表获取变更信息
     * @param tableName 表名
     * @return 变更信息
     */
    public Changes getChangesOfTable(String tableName) {
        if (tableName == null) {
            throw new NullPointerException("表名不能为空！");
        }
        Changes changes = createChangesFromThis();
        List<Change> changesList = getChangesList();
        if (tablesList != null) {
            changes.changesList.addAll(changesList.stream().filter(change -> getTableLetterCase().isEqual(tableName, change.getDataName())).collect(Collectors.toList()));
        }
        return changes;
    }

    /**
     * 根据改变类型获取变更信息
     * @param changeType 变更类型
     * @return 变更信息
     */
    public Changes getChangesOfType(ChangeType changeType) {
        if (changeType == null) {
            throw new NullPointerException("更改类型必须不为空！");
        }
        Changes changes = createChangesFromThis();
        List<Change> changesList = getChangesList();
        changes.changesList.addAll(changesList.stream().filter(change -> changeType.equals(change.getChangeType())).collect(Collectors.toList()));
        return changes;
    }

    private Changes createChangesFromThis() {
        Changes changes = new Changes();
        if (request != null) {
            changes.request = getDuplicatedRequest(request);
        }
        if (tablesList != null) {
            changes.tablesList = new ArrayList<>();
            changes.tablesList.addAll(tablesList.stream().map(Changes::getDuplicatedTable).collect(Collectors.toList()));
        }
        changes.changesList = new ArrayList<>();
        return changes;
    }
}
