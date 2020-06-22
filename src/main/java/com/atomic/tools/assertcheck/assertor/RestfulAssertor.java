package com.atomic.tools.assertcheck.assertor;

import com.atomic.param.Constants;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;

import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @Data 2018/5/19 下午9:52
 * @since 1.0.0
 */
public class RestfulAssertor  extends AbstractAssertor{

    private static final Logger logger = LoggerFactory.getLogger(RestfulAssertor.class);

    @Override
    protected void doAssert(Object result, Map<String, Object> excContext) {
        Response response = (Response) result;
        Assert.assertEquals(response.statusCode(), 200);

        // 移除索引字段
        excContext.remove(Constants.CASE_INDEX);

        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(excContext))) {
            JsonPath path = response.getBody().jsonPath();
            assertJsonPath(excContext, path);
        }
    }
}
