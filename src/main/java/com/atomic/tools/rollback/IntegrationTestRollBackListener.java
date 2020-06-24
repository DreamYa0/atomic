package com.atomic.tools.rollback;

import com.atomic.annotations.AnnotationUtils;
import com.atomic.exception.AnnotationException;
import com.atomic.exception.RollBackException;
import com.atomic.util.TestNGUtils;
import com.atomic.tools.db.Changes;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2017/8/25 22:28
 */
public class IntegrationTestRollBackListener extends TestListenerAdapter {

    private Map<String, Changes> dbNameAndChanges = Maps.newConcurrentMap();

    public IntegrationTestRollBackListener() {
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

    @Override
    public void onTestStart(ITestResult testResult) {
        Changes changes = new Changes();
        if (AnnotationUtils.isRollBackMethod(TestNGUtils.getTestMethod(testResult)) &&
                !AnnotationUtils.isScenario(TestNGUtils.getTestMethod(testResult))) {
            String dbName = AnnotationUtils.getDbName(TestNGUtils.getTestMethod(testResult));
            String[] tableNames = AnnotationUtils.getTableName(TestNGUtils.getTestMethod(testResult));
            // 开启监听，当为@RollBack注解时执行单库,多表数据回滚
            dbNameAndChanges.put(dbName, changes);
            RollBackManager.newInstance().setStartPoint(dbName, changes, tableNames);
        } else if (AnnotationUtils.isRollBackAllMethod(TestNGUtils.getTestMethod(testResult)) &&
                !AnnotationUtils.isScenario(TestNGUtils.getTestMethod(testResult))) {
            // 当为@RollBackAll注解时执行多库,多表数据回滚
            try {
                try {
                    Multimap<String, String> multimap = AnnotationUtils.getDbNameAndTableName(
                            TestNGUtils.getTestMethod(testResult));
                    Set<String> set = multimap.keySet();
                    List<String> stringList = Lists.newArrayList(set);
                    for (String dbName : stringList) {
                        dbNameAndChanges.put(dbName, changes);
                    }
                    if (dbNameAndChanges.size() >= 6) {
                        Reporter.log("-----------------回滚数据库限制不能超过6个！--------------");
                        throw new RollBackException("回滚数据库不能超过6个！");
                    }
                    Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
                    for (Map.Entry<String, Changes> map : entries) {
                        List<String> list = (List<String>) multimap.get(map.getKey());
                        String[] tableNames = new String[list.size()];
                        for (int j = 0; j < list.size(); j++) {
                            tableNames[j] = list.get(j);
                        }
                        RollBackManager.newInstance().setStartPoint(map.getKey(), map.getValue(), tableNames);
                    }
                } catch (AnnotationException e) {
                    e.printStackTrace();
                }
            } catch (RollBackException e) {
                Reporter.log("-----------------回滚数据库限制不能超过6个！--------------");
                throw new RollBackException("回滚数据库限制不能超过6个！");
            }
        }
    }
}