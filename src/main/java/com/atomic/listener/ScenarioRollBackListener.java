package com.atomic.listener;

import com.atomic.config.GlobalConfig;
import com.atomic.exception.RollBackException;
import com.atomic.tools.db.Changes;
import com.atomic.tools.sql.SqlTools;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 通用测试场景数据回滚监听器
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/6/30.
 */
@ThreadSafe
public class ScenarioRollBackListener extends TestListenerAdapter {

    private final String profile;
    private final SqlTools sqlTools;
    private Map<String, Changes> dbNameAndChanges = new ConcurrentHashMap<>();

    public ScenarioRollBackListener() {
        // 加载环境配置文件
        GlobalConfig.load();
        profile = GlobalConfig.getProfile();
        // 获取sqlTools实例
        sqlTools = new SqlTools(profile);
    }

    @Override
    public void onStart(ITestContext testContext) {
        ITestNGMethod[] testNGMethods = testContext.getAllTestMethods();
        Map<String, String[]> tableNameList = RollBack.newInstance().getTableNames4Scenario(testNGMethods, dbNameAndChanges);
        if (dbNameAndChanges.size() > 20) {
            Reporter.log("[ScenarioRollBackListener#startRollBack()]:{} ---> 场景测试回滚数据库不能超过20个！");
            throw new RollBackException("场景测试回滚数据库不能超过20个！");
        }
        if (dbNameAndChanges.size() > 0) {
            // 开启监听
            Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
            for (Map.Entry<String, Changes> map : entries) {
                RollBack.newInstance().setStartPoint(map.getValue(), map.getKey(), profile, tableNameList.get(map.getKey()));
            }
        }
    }

    @Override
    public void onFinish(ITestContext testContext) {
        if (dbNameAndChanges.size() > 0) {
            Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
            for (Map.Entry<String, Changes> map : entries) {
                RollBack.newInstance().setEndPoint(sqlTools, map.getKey(), map.getValue());
            }
        }
    }
}
