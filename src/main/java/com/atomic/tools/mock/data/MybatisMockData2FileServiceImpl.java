package com.atomic.tools.mock.data;

import com.alibaba.fastjson.JSON;
import com.atomic.param.StringUtils;
import com.atomic.tools.mock.dto.MockData4Db;
import com.atomic.tools.mock.dto.MockData4Rpc;
import com.atomic.tools.mock.helper.MockFileHelper;
import com.atomic.util.FileUtils;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author jsy.
 * @title
 * @time 16/10/11.
 */
public class MybatisMockData2FileServiceImpl implements MockDataService<MockData4Db, Invocation> {

    private static final MockDataService instance = new MybatisMockData2FileServiceImpl();

    //*********单例模式****************
    private MybatisMockData2FileServiceImpl() {

    }

    public static MockDataService<MockData4Rpc, Invocation> getInstance() {
        return instance;
    }

    @Override
    public Object getMockData(Invocation invocation) {

        List<Object> dbData = MockContext.getContext().getMockData().getDbData();

        //根据序号取值，order从1开始，数组从0开始，所以减1
        Object dbResult = dbData.get(MockContext.getContext().getDbOrderAndIncrease() - 1);
        try {
            Type genericReturnType = invocation.getMethod().getGenericReturnType();
            return StringUtils.json2Bean("", JSON.toJSONString(dbResult), genericReturnType);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertMockData(MockData4Db mockData) {
        MockContext.getContext().getMockData().getDbData().add(mockData.getDbResult());

    }

    @Override
    public void deleteMockData() {
        FileUtils.removeDir(MockFileHelper.getMockFile(MockContext.getContext().getCaseIndex()));
    }
}
