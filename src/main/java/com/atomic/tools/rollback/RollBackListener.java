package com.atomic.tools.rollback;

import cn.hutool.core.collection.CollUtil;
import com.atomic.exception.AnnotationException;
import com.atomic.exception.RollBackException;
import com.atomic.tools.rollback.db.Changes;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2017/8/25 22:28
 */
public class RollBackListener extends TestListenerAdapter {

    private final ConcurrentMap<String, Changes> dbNameAndChanges = new ConcurrentHashMap<>(16);

    @Override
    public void onStart(ITestContext testContext) {
        ITestNGMethod[] testNGMethods = testContext.getAllTestMethods();
        Map<String, String[]> dbName2TbNames = getDbName2TbNames8TestContext(testNGMethods,
                dbNameAndChanges);
        if (dbNameAndChanges.size() > 10) {
            Reporter.log("回滚数据库不能超过10个！");
            throw new RollBackException("回滚数据库不能超过10个！");
        }

        if (CollUtil.isNotEmpty(dbNameAndChanges)) {
            // 开启监听
            Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
            for (Map.Entry<String, Changes> map : entries) {
                RollBackManager.newInstance().setStartPoint(map.getKey(), map.getValue(),
                        dbName2TbNames.get(map.getKey()));
            }
        }
    }

    @Override
    public void onFinish(ITestContext testContext) {
        if (CollUtil.isNotEmpty(dbNameAndChanges)) {
            Set<Map.Entry<String, Changes>> entries = dbNameAndChanges.entrySet();
            for (Map.Entry<String, Changes> map : entries) {
                RollBackManager.newInstance().setEndPoint(map.getKey(), map.getValue());
            }
        }
    }

    private Map<String, String[]> getDbName2TbNames8TestContext(ITestNGMethod[] testNGMethods,
                                                                Map<String, Changes> dbName2Changes) {
        // 获取监控的数据库表名
        Map<String, String[]> dbName2TbNames = Maps.newHashMap();
        for (ITestNGMethod testNGMethod : testNGMethods) {
            Method method = testNGMethod.getConstructorOrMethod().getMethod();
            if (isRollBackMethod(method)) {
                // 处理 @RollBack 注解
                String dbName = getDbName(testNGMethod.getConstructorOrMethod().getMethod());
                dbName2Changes.put(dbName, new Changes());

                String[] tableNames = getTableName(testNGMethod.getConstructorOrMethod().getMethod());
                dbName2TbNames.put(dbName, tableNames);
            } else if (isRollBackAllMethod(method)) {
                // 处理 @RollBackAll 注解
                // 实现多数据库数据回滚
                Multimap<String, String> multimap = getDbName2TableName(
                        testNGMethod.getConstructorOrMethod().getMethod());
                Set<String> set = multimap.keySet();
                for (String dbName : set) {
                    dbName2Changes.put(dbName, new Changes());
                    List<String> list = (List<String>) multimap.get(dbName);
                    dbName2TbNames.put(dbName, list.toArray(new String[]{}));
                }
            }
        }
        return dbName2TbNames;
    }

    private boolean isRollBackMethod(Method testMethod) throws AnnotationException {
        // 判断测试方法是否添加回滚注解
        Annotation[] annotations = testMethod.getAnnotations();
        if (annotations != null) {
            RollBack rollBack = testMethod.getAnnotation(RollBack.class);
            RollBackAll rollBackAll = testMethod.getAnnotation(RollBackAll.class);
            if (rollBack == null) {
                return false;
            } else if (rollBackAll != null) {
                Reporter.log("@RollBack 和 @RollBackAll 不能同时使用");
                throw new AnnotationException("@RollBack 和 @RollBackAll 不能同时使用");
            } else if (rollBack.enabled()) {
                return !"".equals(rollBack.dbName()) && rollBack.tableName().length != 0;
            } else return rollBack.enabled();
        }
        return false;
    }

    private boolean isRollBackAllMethod(Method method) throws AnnotationException {
        // 检查方法上是否有RollBackAll注解和注解是否开启
        Annotation[] annotations = method.getAnnotations();
        if (annotations != null) {
            RollBack rollBack = method.getAnnotation(RollBack.class);
            RollBackAll rollBackAll = method.getAnnotation(RollBackAll.class);
            if (rollBackAll != null) {
                if (rollBack != null) {
                    Reporter.log("@RollBack 和 @RollBackAll 不能同时使用");
                    throw new AnnotationException("@RollBack 和 @RollBackAll 不能同时使用");
                } else return rollBackAll.enabled();
            }
        }
        return false;
    }

    private String getDbName(Method testMethod) {
        // 从注解中获取数据库名
        RollBack annotation = testMethod.getAnnotation(RollBack.class);
        return annotation.dbName();
    }

    private String[] getTableName(Method testMethod) {
        // 从注解中获取表名
        RollBack annotation = testMethod.getAnnotation(RollBack.class);
        return annotation.tableName();
    }

    private Multimap<String, String> getDbName2TableName(Method method) throws AnnotationException {
        // 处理多库多表时，表名和库名,参数示例：库名.表名
        RollBackAll rollBackAll = method.getAnnotation(RollBackAll.class);
        String[] dbAndTable = rollBackAll.dbAndTable();
        Multimap<String, String> multimap = ArrayListMultimap.create();
        for (int i = 0; i < dbAndTable.length; i++) {
            String[] dbNameAndTableName = dbAndTable[i].split("\\.");
            if (dbNameAndTableName.length != 2) {
                Reporter.log("库名和表名方式错误！");
                throw new AnnotationException("库名和表名方式错误！");
            }
            multimap.put(dbNameAndTableName[0], dbNameAndTableName[1]);
        }
        return multimap;
    }
}
