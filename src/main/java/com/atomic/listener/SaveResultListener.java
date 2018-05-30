package com.atomic.listener;

import com.alibaba.fastjson.JSON;
import com.atomic.config.GlobalConfig;
import com.atomic.exception.ListenerException;
import com.atomic.param.Constants;
import com.atomic.param.HandleMethodName;
import com.atomic.param.ParamUtils;
import com.atomic.param.entity.QaProject;
import com.atomic.param.entity.QaResult;
import com.atomic.util.CIDbUtils;
import com.google.gson.Gson;
import io.restassured.response.Response;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * @author dreamyao
 * @version 1.2.0
 * @title 测试数据入库监听器
 * @Data 2018/05/30 10:48
 */
@ThreadSafe
public class SaveResultListener extends TestListenerAdapter {

    /**
     * 项目ID
     */
    private final Integer projectId;
    /**
     * 测试用例执行者
     */
    private final String runAuthor;
    /**
     * 用例执行轮数
     */
    private AtomicInteger round = new AtomicInteger(1);

    public SaveResultListener() {
        // 加载环境配置文件
        GlobalConfig.load();
        // 项目名称
        String projectName = GlobalConfig.projectName;
        this.runAuthor = GlobalConfig.runner;
        if ("".equals(projectName) || "".equals(runAuthor)) {
            throw new ListenerException("请在test.properties中填写：projectName、runner或修改其默认值！");
        }
        //TODO 2.0版本SQL会换成调用SOA服务接口
        String queryProject = "select * from qa_project where project_name= ?";
        Object[] queryProjectParam = {projectName};
        QaProject qaProject = CIDbUtils.queryQaProjectValue(queryProject, queryProjectParam);
        if (qaProject == null) {
            String insertProject = "insert into qa_project (project_name,project_status,author,create_time,update_time) values (?,?,?,?,?)";
            Object[] insertProjectParam = {projectName, 1, runAuthor, new Date(), new Date()};
            CIDbUtils.updateValue(insertProject, insertProjectParam);
            QaProject queryQaProject = CIDbUtils.queryQaProjectValue(queryProject, queryProjectParam);
            projectId = queryQaProject.getId();
        } else {
            projectId = qaProject.getId();
        }
        String sql = "select * from qa_result where project_id= ? order by create_time desc";
        Object[] param = {projectId};
        QaResult qaMethod = CIDbUtils.queryQaMethodValue(sql, param);
        if (qaMethod != null) {
            round.set(round.get() + qaMethod.getRound());
        }
    }

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

    /**
     * 把测试结果写入数据库中
     * @param result
     */
    private void testResultIntoDatabase(ITestResult result) {
        String methodName = null;
        String className = null;
        try {
            int testStatus = result.getStatus();
            methodName = HandleMethodName.getTestMethodName(result);
            className = HandleMethodName.getTestClassName(result);
            Map<String, Object> context = ParamUtils.getParamContext(result);
            Object methodsReturn = context.get(Constants.RESULT_NAME);
            String caseName;
            if (context.get(Constants.CASE_NAME) == null) {
                caseName = context.get(Constants.EXCEL_DESC).toString();
            } else {
                caseName = context.get(Constants.CASE_NAME).toString();
            }
            String expectedReturn;
            if (context.get(Constants.EXPECTED_RESULT) == null) {
                expectedReturn = null;
            } else {
                expectedReturn = context.get(Constants.EXPECTED_RESULT).toString();
            }
            Gson gson = new Gson();
            if (context.get(Constants.PARAMETER_NAME_) != null) {
                Object[] methodsParameter = (Object[]) context.get(Constants.PARAMETER_NAME_);
                String jsonParam;
                String jsonReturn;
                try {
                    jsonParam = gson.toJson(methodsParameter[0]);
                } catch (Exception e) {
                    jsonParam = JSON.toJSONString(methodsParameter[0]);
                }
                try {
                    jsonReturn = gson.toJson(methodsReturn);
                } catch (Exception e) {
                    jsonReturn = JSON.toJSONString(methodsReturn);
                }
                insertData(projectId, className, methodName, caseName, jsonParam
                        , jsonReturn, expectedReturn, testStatus, round.get(), runAuthor);
            } else if (context.get("m_parameter_0") != null) {
                Object methodsParameters = context.get("m_parameter_0");
                if (methodsReturn instanceof Response) {
                    Response response = (Response) methodsReturn;
                    insertData(projectId, className, methodName, caseName, methodsParameters.toString()
                            , response.body().asString(), expectedReturn, testStatus, round.get(), runAuthor);
                } else {
                    insertData(projectId, className, methodName, caseName, methodsParameters.toString()
                            , methodsReturn.toString(), expectedReturn, testStatus, round.get(), runAuthor);
                }
            } else {
                String jsonReturn;
                try {
                    jsonReturn = gson.toJson(methodsReturn);
                } catch (Exception e) {
                    jsonReturn = JSON.toJSONString(methodsReturn);
                }
                insertData(projectId, className, methodName, caseName, "{}"
                        , jsonReturn, expectedReturn, testStatus, round.get(), runAuthor);
            }
        } catch (Exception e) {
            System.out.println("---------------------" + className + "#" + methodName + "--------------------");
            e.printStackTrace();
        }
    }

    private void insertData(Integer projectId, String className, String methodName, String caseName
            , String methodsParameter, String methodsReturn, String expectedReturn
            , int testStatus, Integer round, String runAuthor) {
        if (caseName != null) {
            String sql = "insert into qa_result (project_id,class_name,methods_name,case_name,methods_parameter,methods_return,expected_return,is_pass,round,run_author,create_time) values(?,?,?,?,?,?,?,?,?,?,?)";
            Object[] params = {projectId, className, methodName, caseName
                    , methodsParameter, methodsReturn, expectedReturn
                    , testStatus, round, runAuthor, new Date()};
            CIDbUtils.updateValue(sql, params);
        }
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getRunAuthor() {
        return runAuthor;
    }
}
