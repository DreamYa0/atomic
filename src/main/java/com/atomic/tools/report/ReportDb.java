package com.atomic.tools.report;

import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import com.atomic.util.DataSourceUtils;
import org.testng.Reporter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by DreamYao on 2016/12/17.
 * 数据库连接工具类，用作对数据库进行查询操作，目前只提供查询操作，
 * 后期根据需要再提供更新、插入、删除操作
 * 此类只供持续集成项目调用，接口测试项目不得调用
 */
public final class ReportDb {

    private ReportDb() {

    }

    public static Entity query(String sqlString, Object[] params) {
        // 返回数据库响应查询语句结果的Json数据封装结果值,这个类负责在数据库中进行数据的查询，和对查询结果进行数据封装
        try {
            DataSource dataSource = DataSourceUtils.getDataSource("atomic_autotest");
            List<Entity> query = DbUtil.use(dataSource).query(sqlString, params);
            return query.get(0);

        } catch (SQLException e) {
            Reporter.log(String.format("异常信息：%s",e.getMessage()), true);
            throw new RuntimeException(e);
        }
    }

    public static void insert(String sqlString, Object[] params) {
        try {
            DbUtil.use(DataSourceUtils.getDataSource("atomic_autotest")).execute(sqlString, params);
        } catch (SQLException e) {
            Reporter.log(String.format("异常信息：%s",e.getMessage()), true);
        }
    }
}
