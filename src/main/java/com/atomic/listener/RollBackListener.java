package com.atomic.listener;

import com.atomic.annotations.AnnotationUtils;
import com.atomic.config.GlobalConfig;
import com.atomic.exception.AnnotationException;
import com.atomic.exception.RollBackException;
import com.atomic.param.MethodMetaUtils;
import com.atomic.tools.db.Changes;
import com.atomic.tools.sql.SqlTools;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.springframework.util.CollectionUtils;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据回滚监听器
 * @author dreamyao
 * @version 1.0           Created by dreamyao on 2017/6/5.
 */
@ThreadSafe
public class RollBackListener extends TestListenerAdapter {

    private final SqlTools sqlTools;
    private final String profile;
    private Map<String, Changes> dbNameAndChanges = Maps.newConcurrentMap();

    public RollBackListener() {
        //加载环境配置文件
        GlobalConfig.load();
        profile = GlobalConfig.getProfile();
        //获取sqlTools实例
        sqlTools = new SqlTools(profile);
    }

    /**
     * 开启数据回滚监听
     * @param testResult
     */
    @Override
    public void onTestStart(ITestResult testResult) {
        //实现单库多表数据回滚
        if (AnnotationUtils.isRollBackMethod(MethodMetaUtils.getTestMethod(testResult)) && !AnnotationUtils.isScenario(MethodMetaUtils.getTestMethod(testResult))) {
            startRollBack(testResult);//开启监听，当为@RollBack注解时执行单库,多表数据回滚
        } else if (AnnotationUtils.isRollBackAllMethod(MethodMetaUtils.getTestMethod(testResult)) && !AnnotationUtils.isScenario(MethodMetaUtils.getTestMethod(testResult))) {
            //实现多库多表数据回滚
            try {
                startRollBackAll(testResult);//当为@RollBackAll注解时执行多库,多表数据回滚
            } catch (RollBackException e) {
                Reporter.log("-----------------回滚数据库限制不能超过5个！--------------");
                throw new RollBackException("回滚数据库限制不能超过5个！");
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        finishRollBack();
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        finishRollBack();
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        finishRollBack();
    }

    /**
     * 当为@RollBack注解时执行单库,多表数据回滚
     * @param testResult
     */
    private void startRollBack(ITestResult testResult) {
        String dbName = AnnotationUtils.getDbName(MethodMetaUtils.getTestMethod(testResult));
        String[] tableNames = AnnotationUtils.getTableName(MethodMetaUtils.getTestMethod(testResult));
        //开启监听
        dbNameAndChanges.putIfAbsent(dbName, new Changes());
        Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
        for (Map.Entry<String, Changes> entry : entries) {
            RollBack.newInstance().setStartPoint(entry.getValue(), entry.getKey(), profile, tableNames);
        }

    }

    /**
     * 当为@RollBackAll注解时执行多库,多表数据回滚
     * @param testResult
     */
    private void startRollBackAll(ITestResult testResult) throws RollBackException {
        //实现多数据库数据回滚
        try {
            Multimap<String, String> multimap = AnnotationUtils.getDbNameAndTableName(MethodMetaUtils.getTestMethod(testResult));
            Set<String> set = multimap.keySet();
            List<String> stringList = Lists.newArrayList();
            stringList.addAll(set);
            for (String dbName : stringList) {
                dbNameAndChanges.put(dbName, new Changes());
            }
            if (dbNameAndChanges.size() >= 6) {
                Reporter.log("-----------------回滚数据库限制不能超过5个！--------------");
                throw new RollBackException("回滚数据库不能超过5个！");
            }
            Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
            for (Map.Entry<String, Changes> entry : entries) {
                List<String> list = (List<String>) multimap.get(entry.getKey());
                String[] tableNames = new String[list.size()];
                for (int j = 0; j < list.size(); j++) {
                    tableNames[j] = list.get(j);
                }
                RollBack.newInstance().setStartPoint(entry.getValue(), entry.getKey(), profile, tableNames);
            }
        } catch (AnnotationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭数据回滚监听并执行数据回滚
     */
    private void finishRollBack() {
        if (!CollectionUtils.isEmpty(dbNameAndChanges)) {
            Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
            for (Map.Entry<String, Changes> map : entries) {
                RollBack.newInstance().setEndPoint(sqlTools, map.getKey(), map.getValue());
            }
        }
    }
}
