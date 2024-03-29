package com.atomic.tools.assertcheck.assertor;

import com.atomic.param.Constants;
import com.atomic.util.GsonUtils;
import io.restassured.path.json.JsonPath;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @date 2018/5/19 下午9:16
 * @since 1.0.0
 */
public class UnitTestAssertor  extends AbstractAssertor {

    @Override
    protected void doAssert(Object result, Map<String, Object> excContext) {
        // 移除索引字段
        excContext.remove(Constants.CASE_INDEX);

        // 当有预期结果时执行详细断言
        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(excContext))) {
            String jsonResult = GsonUtils.getGson().toJson(result);
            JsonPath resultPath = JsonPath.from(jsonResult);
            assertJsonPath(excContext, resultPath);
        }
    }
}
