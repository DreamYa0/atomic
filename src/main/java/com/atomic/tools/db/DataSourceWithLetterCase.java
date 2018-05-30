package com.atomic.tools.db;

import com.atomic.tools.db.lettercase.LetterCase;
import com.atomic.tools.db.lettercase.WithLetterCase;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 一个数据源连接到数据库和字母的情况。
 */
public class DataSourceWithLetterCase implements DataSource, WithLetterCase {

    private final DataSource dataSource;

    private final LetterCase tableLetterCase;

    private final LetterCase columnLetterCase;

    private final LetterCase primaryKeyLetterCase;

    public DataSourceWithLetterCase(DataSource dataSource, LetterCase tableLetterCase,
                                    LetterCase columnLetterCase, LetterCase primaryKeyLetterCase) {

        this.dataSource = dataSource;
        this.tableLetterCase = tableLetterCase;
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

    @Override
    public LetterCase getTableLetterCase() {
        return tableLetterCase;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }
}
