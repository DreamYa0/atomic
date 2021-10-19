package com.atomic.tools.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ResourceCDN;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
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
        extent.attachReporter(createHtmlReporter(projectName));
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
        return htmlReporter;
    }
}
