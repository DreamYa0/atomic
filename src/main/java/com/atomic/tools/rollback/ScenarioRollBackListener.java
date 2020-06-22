package com.atomic.tools.rollback;

import com.atomic.exception.RollBackException;
import com.atomic.tools.db.Changes;
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

    private Map<String, Changes> dbNameAndChanges = new ConcurrentHashMap<>();

    public ScenarioRollBackListener() {

    }

    @Override
    public void onStart(ITestContext testContext) {
        ITestNGMethod[] testNGMethods = testContext.getAllTestMethods();
        Map<String, String[]> tableNameList = RollBackManager.newInstance().getTableNames4Scenario(testNGMethods,
                dbNameAndChanges);
        if (dbNameAndChanges.size() > 20) {
            Reporter.log("场景测试回滚数据库不能超过20个！");
            throw new RollBackException("场景测试回滚数据库不能超过20个！");
        }
        if (dbNameAndChanges.size() > 0) {
            // 开启监听
            Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
            for (Map.Entry<String, Changes> map : entries) {
                RollBackManager.newInstance().setStartPoint(map.getKey(), map.getValue(), tableNameList.get(map.getKey()));
            }
        }
    }

    @Override
    public void onFinish(ITestContext testContext) {
        if (dbNameAndChanges.size() > 0) {
            Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
            for (Map.Entry<String, Changes> map : entries) {
                RollBackManager.newInstance().setEndPoint(map.getKey(), map.getValue());
            }
        }
    }
}
