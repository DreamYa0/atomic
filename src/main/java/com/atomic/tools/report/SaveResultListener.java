package com.atomic.tools.report;

import cn.hutool.db.Entity;
import com.alibaba.fastjson.JSON;
import com.atomic.config.ConfigConstants;
import com.atomic.config.AtomicConfig;
import com.atomic.param.Constants;
import com.atomic.util.GsonUtils;
import com.atomic.util.TestNGUtils;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author dreamyao
 * @version 1.2.0
 * @title 测试数据入库监听器
 * @date 2018/05/30 10:48
 */
@ThreadSafe
public class SaveResultListener extends TestListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SaveResultListener.class);
    /**
     * 项目ID
     */
    private volatile Integer projectId;
    /**
     * 测试用例执行者
     */
    private volatile String runAuthor;
    /**
     * 用例执行轮数
     */
    private AtomicInteger round = new AtomicInteger(1);

    @Override
    public void onTestSuccess(ITestResult result) {
        testResultIntoDatabase(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        testResultIntoDatabase(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        testResultIntoDatabase(result);
    }

    private void testResultIntoDatabase(ITestResult result) {
        // 把测试结果写入数据库中
        init();
        String methodName = null;
        String className = null;
        try {
            int testStatus = result.getStatus();
            methodName = HandleMethodName.getTestMethodName(result);
            className = HandleMethodName.getTestClassName(result);
            Map<String, Object> context = TestNGUtils.getParamContext(result);
            Object methodsReturn = context.get(Constants.RESULT_NAME);
            String caseName;
            if (context.get(Constants.CASE_NAME) == null) {
                caseName = context.get(Constants.EXCEL_DESC).toString();
            } else {
                caseName = context.get(Constants.CASE_NAME).toString();
            }
            String expectedReturn;
            // TODO: 2018/6/12 预期结果从excel sheet页获取
            if (context.get(Constants.EXPECTED_RESULT) == null) {
                expectedReturn = null;
            } else {
                expectedReturn = context.get(Constants.EXPECTED_RESULT).toString();
            }

            Object param = context.get(Constants.PARAMETER_NAME_);

            if (Objects.isNull(param) && Objects.isNull(methodsReturn)) {
                return;
            }

            if (Objects.nonNull(param) && Boolean.TRUE.equals(param instanceof HashMap)) {

                if (methodsReturn instanceof Response) {
                    Response response = (Response) methodsReturn;
                    insertData(projectId, className, methodName, caseName, param.toString(),
                            response.body().asString(), expectedReturn, testStatus, round.get(), runAuthor);
                } else {
                    insertData(projectId, className, methodName, caseName, param.toString(),
                            methodsReturn.toString(), expectedReturn, testStatus, round.get(), runAuthor);
                }
                return;

            }

            Object[] methodsParameter = (Object[]) param;

            if (Objects.nonNull(param) && methodsParameter.length > 0) {

                String jsonParam;
                String jsonReturn;
                try {
                    jsonParam = GsonUtils.getGson().toJson(methodsParameter[0]);
                } catch (Exception e) {
                    jsonParam = JSON.toJSONString(methodsParameter[0]);
                }
                try {
                    jsonReturn = GsonUtils.getGson().toJson(methodsReturn);
                } catch (Exception e) {
                    jsonReturn = JSON.toJSONString(methodsReturn);
                }
                insertData(projectId, className, methodName, caseName, jsonParam, jsonReturn, expectedReturn,
                        testStatus, round.get(), runAuthor);
                return;
            }

            String jsonReturn;
            try {
                jsonReturn = GsonUtils.getGson().toJson(methodsReturn);
            } catch (Exception e) {
                jsonReturn = JSON.toJSONString(methodsReturn);
            }
            insertData(projectId, className, methodName, caseName, "{}", jsonReturn,
                    expectedReturn, testStatus, round.get(), runAuthor);

        } catch (Exception e) {
            System.out.println("------------------- " + className + "#" + methodName + " -------------------");
            logger.error("保存测试用例失败", e);
        }
    }

    private void init() {
        // 加载环境配置文件
        // 项目名称
        String projectName = AtomicConfig.getStr(ConfigConstants.PROJECT_NAME);
        this.runAuthor = AtomicConfig.getStr(ConfigConstants.RUNNER);
        if (StringUtils.isEmpty(projectName)) {
            projectName = "default";
        }
        if (StringUtils.isEmpty(runAuthor)) {
            runAuthor = "default";
        }
        String queryProject = "select * from autotest_project where project_name= ?";
        Object[] queryProjectParam = {projectName};
        Entity value = ReportDb.query(queryProject, queryProjectParam);
        if (value == null) {
            String insertProject = "insert into autotest_project (project_name,project_status,author," +
                    "create_time,update_time) values (?,?,?,?,?)";
            Object[] insertProjectParam = {projectName, 1, runAuthor, new Date(), new Date()};
            ReportDb.insert(insertProject, insertProjectParam);
            Entity entity = ReportDb.query(queryProject, queryProjectParam);
            projectId = entity.getInt("id");
        } else {
            projectId = value.getInt("id");
        }
        String sql = "select * from autotest_result where project_id= ? order by create_time desc";
        Object[] param = {projectId};
        Entity entity = ReportDb.query(sql, param);
        if (Objects.nonNull(entity) && Objects.equals(1, round.get())) {
            round.set(round.get() + entity.getInt("round"));
        }
    }

    private void insertData(Integer projectId, String className, String methodName, String caseName
            , String methodsParameter, String methodsReturn, String expectedReturn
            , int testStatus, Integer round, String runAuthor) {
        if (caseName != null) {
            String sql = "insert into autotest_result (project_id,class_name,methods_name,case_name," +
                    "methods_parameter,methods_return,expected_return,is_pass,round," +
                    "run_author,create_time) values(?,?,?,?,?,?,?,?,?,?,?)";
            Object[] params = {projectId, className, methodName, caseName
                    , methodsParameter, methodsReturn, expectedReturn
                    , testStatus, round, runAuthor, new Date()};
            ReportDb.insert(sql, params);
        }
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getRunAuthor() {
        return runAuthor;
    }
}
