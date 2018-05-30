package com.atomic.tools;

import com.atomic.config.GlobalConfig;
import com.atomic.tools.dubbo.DubboServiceFactory;
import com.atomic.tools.sql.SqlTools;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutionException;

/**
 * 按TestMode初始化各种tools,并缓存
 * 适用于自动化回归时使用
 */
@Deprecated
public class ToolsFactory {

    private static Cache<String, ToolsFactory> toolsFactoryCache = CacheBuilder.newBuilder().build();
    private DubboServiceFactory dubboServiceFactory;
    private SqlTools sqlTools;

    private ToolsFactory(String profile) {
        dubboServiceFactory = new DubboServiceFactory(profile);
        sqlTools = new SqlTools(profile);
    }

    public static ToolsFactory getInstance() {
        return getInstance(GlobalConfig.getProfile());
    }

    public static ToolsFactory getInstance(String profile) {
        /*if (toolsFactoryMap == null) {
            toolsFactoryMap = new HashMap<TestMode, ToolsFactory>();
        }
        ToolsFactory toolsFactory = toolsFactoryMap.get(mode);
        if (toolsFactory == null) {
            toolsFactory = new ToolsFactory(mode);
            toolsFactoryMap.put(mode, toolsFactory);
        }*/
        ToolsFactory toolsFactory = null;
        try {
            toolsFactory = toolsFactoryCache.get(profile, () -> new ToolsFactory(profile));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return toolsFactory;
    }

    public DubboServiceFactory getDubboServiceFactory() {
        return dubboServiceFactory;
    }

    public SqlTools getSqlTools() {
        return sqlTools;
    }

    /**
     * 一次性创建5个数据库连接
     * @param profile
     * @return
     */
    public ThreadLocal<SqlTools> getSqlToolFive(String profile) {
        dubboServiceFactory = new DubboServiceFactory(profile);
        SqlTools sqlToolOne = new SqlTools(profile);
        SqlTools sqlToolTwo = new SqlTools(profile);
        SqlTools sqlToolThree = new SqlTools(profile);
        SqlTools sqlToolFour = new SqlTools(profile);
        SqlTools sqlToolFive = new SqlTools(profile);
        ThreadLocal<SqlTools> sqlToolsThreadLocal = new ThreadLocal<>();
        sqlToolsThreadLocal.set(sqlToolOne);
        sqlToolsThreadLocal.set(sqlToolTwo);
        sqlToolsThreadLocal.set(sqlToolThree);
        sqlToolsThreadLocal.set(sqlToolFour);
        sqlToolsThreadLocal.set(sqlToolFive);
        return sqlToolsThreadLocal;
    }


}
