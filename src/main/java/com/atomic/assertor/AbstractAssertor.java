package com.atomic.assertor;

import com.atomic.param.Constants;
import com.atomic.param.TestNGUtils;
import com.atomic.util.ExcelUtils;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.ITestResult;

import java.util.List;
import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @date 2018/6/3 下午3:57
 * @since 1.0.0
 */
public abstract class AbstractAssertor implements Assertor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAssertor.class);

    /**
     * 断言准备
     * @param testResult 测试上下文
     * @param result     返回结果
     * @param instance   测试类实列
     */
    @Override
    public void assertResult(ITestResult testResult, Object result, Object instance) {
        try {
            ExcelUtils excelUtil = new ExcelUtils();
            List<Map<String, Object>> list = excelUtil.readDataByRow(testResult,instance,"exceptResult");

            if (Boolean.FALSE.equals(CollectionUtils.isEmpty(list))) {
                Map<String, Object> param = TestNGUtils.getParamContext(testResult);
                if (Boolean.FALSE.equals(CollectionUtils.isEmpty(param))) {
                    doAssert(result, list.get(Integer.valueOf(param.get(Constants.CASE_INDEX).toString()) - 1));
                }
            }

        } catch (Exception e) {
            logger.info("---------------- excel中名称为exceptResult的sheet页不存在或值为空，请进行手动断言！ ----------------");
        }
    }

    protected void assertJsonPath(Map<String, Object> excContext, JsonPath resultPath) {
        // 执行断言操作
        excContext.forEach((jsonPath, expecValue) -> {

            if (Boolean.FALSE.equals(StringUtils.isEmpty(jsonPath))) {

                String actualResult = resultPath.getString(jsonPath);
                if (StringUtils.isEmpty(actualResult)) {
                    Assert.assertNotNull(actualResult, String.format("当前 JsonPath 路径不存在或 Result 返回值错误，JsonPath 路径为：%s", jsonPath));
                } else {
                    Assert.assertEquals(actualResult, expecValue);
                }
            }
        });
    }

    /**
     * 执行断言
     * @param result     返回结果
     * @param excContext 预期结果上下文
     */
    protected abstract void doAssert(Object result, Map<String, Object> excContext);
}
