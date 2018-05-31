package com.atomic.assertor;

import com.atomic.param.Constants;
import com.atomic.param.ParamUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @Data 2018/5/19 下午9:16
 * @since 1.0.0
 */
public class UnitTestAssertor implements Assertor {

    @Override
    public void assertResult(Object result, Map<String, Object> context) {

        if (ParamUtils.isExpectedResultNoNull(context)) {
            Gson gson = new Gson();
            String jsonResult = gson.toJson(result);
            JsonPath resultPath = JsonPath.from(jsonResult);
            String expectedResult = (String) context.get(Constants.EXPECTED_RESULT);

            // 解析预期断言信息
            Map<String, String> expecResultMap = Maps.newHashMap();
            Iterator<String> iterator = Splitter.on(",").split(expectedResult).iterator();
            while (iterator.hasNext()) {
                String expecValue = iterator.next();
                List<String> list = Lists.newArrayList();
                Splitter.on("=").split(expecValue).forEach(list::add);
                expecResultMap.put(list.get(0), list.get(1));
            }

            // 执行断言操作
            expecResultMap.forEach((jsonPath, expecValue) -> {
                String actualResult = resultPath.getString(jsonPath);
                Assert.assertEquals(actualResult, expecValue);
            });
        }
    }
}
