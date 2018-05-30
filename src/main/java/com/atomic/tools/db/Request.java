package com.atomic.tools.db;


import com.atomic.tools.db.lettercase.LetterCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数据库中获取值的请求
 */
public class Request extends AbstractDbData<Request> {

    /**
     * 获取值的SQL请求
     */
    private String request;
    /**
     * SQL请求的参数
     */
    private Object[] parameters;

    public Request() {
        super(Request.class, DataType.REQUEST);
    }

    public Request(Source source, String request, Object... parameters) {
        super(Request.class, DataType.REQUEST, source);
        setRequest(request);
        this.parameters = parameters;
    }

    public Request(DataSource dataSource, String request, Object... parameters) {
        super(Request.class, DataType.REQUEST, dataSource);
        setRequest(request);
        this.parameters = parameters;
    }

    public String getRequest() {
        return request;
    }

    public Request setRequest(String request) {
        if (request == null) {
            throw new NullPointerException("request can not be null");
        }

        this.request = request;
        return this;
    }

    public Object[] getParameters() {
        if (parameters == null) {
            return null;
        }
        return parameters.clone();
    }

    public Request setParameters(Object... parameters) {
        this.parameters = parameters;
        return this;
    }

    public Request setPksName(String... pksName) {
        List<String> pksNameList = new ArrayList<>();
        pksNameList.addAll(Arrays.asList(pksName));
        super.setPksNameList(pksNameList);
        return this;
    }

    /**
     * 从SQL请求中收集来自的列名称
     * @param resultSet
     * @throws SQLException
     */
    private void collectColumnsNameFromResultSet(ResultSet resultSet) throws SQLException {
        LetterCase letterCase = getColumnLetterCase();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        List<String> columnsNameList = new ArrayList<>();
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            String columnName = resultSetMetaData.getColumnLabel(i);
            columnsNameList.add(letterCase.convert(columnName));
        }
        setColumnsNameList(columnsNameList);
        controlIfAllThePksNameExistInTheColumns();
    }

    /**
     * 特定的执行
     * @param connection {@link Connection} to the database provided by {@link AbstractDbData#load()} private method.
     * @throws NullPointerException If the {@link #request} field is {@code null}.
     * @throws SQLException         SQL Exception.
     * @see AbstractDbData#loadImpl(Connection)
     */
    @Override
    protected void loadImpl(Connection connection) throws SQLException {
        if (request == null) {
            throw new NullPointerException("request can not be null");
        }

        try (PreparedStatement statement = connection.prepareStatement(request)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                collectColumnsNameFromResultSet(resultSet);
                collectRowsFromResultSet(resultSet);
            }
        }
    }
}
