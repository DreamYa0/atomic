package com.atomic.tools.db;

import com.atomic.tools.db.lettercase.LetterCase;
import com.atomic.tools.db.lettercase.WithLetterCase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 这个类表示来自数据库的元素(一个{@link AbstractDbElement}或{@link Change })。
 * 因此，这个类包含:使用{@link #getSource()}和{@link #getSource}
 * (其中一个需要在加载数据之前被设置)的访问数据库的方法。
 */
public abstract class AbstractDbElement<D extends AbstractDbElement<D>> implements DbElement, WithLetterCase {

    /**
     * 元素的类
     */
    protected final D myself;
    /**
     * 数据来源
     */
    private Source source;
    /**
     * 数据源
     */
    private DataSource dataSource;
    /**
     * 表的字母
     */
    private LetterCase tableLetterCase;
    /**
     * 列的字母
     */
    private LetterCase columnLetterCase;
    /**
     * 主键的字母大小写
     */
    private LetterCase primaryKeyLetterCase;

    /**
     * 默认构造函数
     * @param selfType
     */
    AbstractDbElement(Class<D> selfType) {
        myself = selfType.cast(this);
        setLetterCases();
    }

    AbstractDbElement(Class<D> selfType, Source source) {
        this(selfType);
        this.source = source;
        setLetterCases();
    }

    AbstractDbElement(Class<D> selfType, DataSource dataSource) {
        this(selfType);
        this.dataSource = dataSource;
        setLetterCases();
    }

    /**
     * 从一个连接返回目录
     * @param connection 连接
     * @return 来自连接的目录
     * @throws SQLException SQL Exception
     */
    protected static String getCatalog(Connection connection) throws SQLException {
        try {
            return connection.getCatalog();
        } catch (SQLException exception) {
            throw exception;
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * 从一个连接返回模式
     * @param connection 连接
     * @return 来自连接的模式
     * @throws SQLException SQL Exception
     */
    protected static String getSchema(Connection connection) throws SQLException {
        try {
            return connection.getSchema();
        } catch (SQLException exception) {
            throw exception;
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * 从参数中设置信息
     * @param tableLetterCase      表的字母
     * @param columnLetterCase     列的字母
     * @param primaryKeyLetterCase 主键的字母大小写
     * @return 实例
     */
    D setLetterCases(LetterCase tableLetterCase, LetterCase columnLetterCase, LetterCase primaryKeyLetterCase) {
        this.tableLetterCase = tableLetterCase;
        this.columnLetterCase = columnLetterCase;
        this.primaryKeyLetterCase = primaryKeyLetterCase;
        return myself;
    }

    private void setLetterCases() {
        if (dataSource instanceof WithLetterCase) {
            WithLetterCase withLetterCase = (WithLetterCase) dataSource;
            tableLetterCase = withLetterCase.getTableLetterCase();
            columnLetterCase = withLetterCase.getColumnLetterCase();
            primaryKeyLetterCase = withLetterCase.getPrimaryKeyLetterCase();
        } else if (source instanceof WithLetterCase) {
            WithLetterCase withLetterCase = (WithLetterCase) source;
            tableLetterCase = withLetterCase.getTableLetterCase();
            columnLetterCase = withLetterCase.getColumnLetterCase();
            primaryKeyLetterCase = withLetterCase.getPrimaryKeyLetterCase();
        } else {
            tableLetterCase = LetterCase.TABLE_DEFAULT;
            columnLetterCase = LetterCase.COLUMN_DEFAULT;
            primaryKeyLetterCase = LetterCase.PRIMARY_KEY_DEFAULT;
        }
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

    public Source getSource() {
        return source;
    }

    public D setSource(Source source) {
        if (source == null) {
            throw new NullPointerException("source must be not null");
        }
        this.source = source;
        this.dataSource = null;
        setLetterCases();
        return myself;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public D setDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource must be not null");
        }
        this.source = null;
        this.dataSource = dataSource;
        setLetterCases();
        return myself;
    }

    protected Connection getConnection() throws SQLException {
        if (dataSource == null && source == null) {
            throw new NullPointerException("connection or dataSource must be not null");
        }

        // Get a Connection differently, depending if it is a DataSource or a Source.
        if (dataSource != null) {
            return dataSource.getConnection();
        } else {
            return DriverManager.getConnection(source.getUrl(), source.getUser(), source.getPassword());
        }
    }
}
