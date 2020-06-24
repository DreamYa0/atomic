package com.atomic.tools.report;

import cn.hutool.db.Entity;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ResourceCDN;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentXReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;


/**
 * 通用测试框架与平台数据对接工具类
 * @author dreamyao
 * @version 1.0
 */
final class ExtentManager {

    private static ExtentReports extent;

    private ExtentManager(String projectName) {
        createInstance(projectName);
    }

    private ExtentManager() {
    }

    public static ExtentManager newInstance() {
        return new ExtentManager();
    }

    public static ExtentReports getInstance(String projectName) {
        if (extent == null) {
            new ExtentManager(projectName);
        }
        return extent;
    }

    private void createInstance(String projectName) {
        extent = new ExtentReports();
        // extent.attachReporter(createHtmlReporter(projectName), createExtentXReporter(projectName));
        extent.attachReporter(createHtmlReporter(projectName));
        // extent.attachReporter(createHtmlReporter(projectName));
        extent.setReportUsesManualConfiguration(true);
    }

    private ExtentHtmlReporter createHtmlReporter(String projectName) {
        //文件夹不存在的话进行创建
        String OUTPUT_FOLDER = "src/test/resources/report/";
        File reportDir = new File(OUTPUT_FOLDER);
        if (!reportDir.exists() && !reportDir.isDirectory()) {
            reportDir.mkdir();
        }
        String FILE_NAME = "report.html";
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(OUTPUT_FOLDER + FILE_NAME);
        htmlReporter.config().setDocumentTitle("自动化测试报告");
        htmlReporter.config().setReportName(projectName);
        htmlReporter.config().setChartVisibilityOnOpen(true);
        htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
        htmlReporter.config().setTheme(Theme.DARK);
        htmlReporter.config().setEncoding("UTF-8");
        htmlReporter.config().setLevel(Status.INFO);
        htmlReporter.config().setProtocol(Protocol.HTTP);
        htmlReporter.config().setResourceCDN(ResourceCDN.EXTENTREPORTS);
        // TODO
        htmlReporter.config().setExtentXUrl("http://10.199.5.131:1337/#/");
        return htmlReporter;
    }

    private ExtentXReporter createExtentXReporter(String projectName) {
        // TODO
        ExtentXReporter extentx = new ExtentXReporter("10.199.5.130", 27017);
        extentx.config().setProjectName(projectName);
        extentx.config().setReportName(projectName + "#" + countBuild(projectName));
        // TODO
        extentx.config().setServerUrl("http://10.199.5.131:1337/#/");
        return extentx;
    }

    String countBuild(String projectName) {
        // 根据项目名称统计项目构建次数
        Integer projectId = getProjectId(projectName);
        String sql = "select * from autotest_result where project_id= ? order by create_time desc";
        Object[] param = {projectId};
        Entity entity = ReportDb.query(sql, param);
        if (entity != null) {
            return entity.getStr("round");
        }
        return "1";
    }

    private Integer getProjectId(String projectName) {
        // 从数据库中获取对应项目的ID
        String queryProject = "select * from autotest_project where project_name= ?";
        Object[] queryProjectParam = {projectName};
        Entity entity = ReportDb.query(queryProject, queryProjectParam);
        return entity.getInt("id");
    }
}