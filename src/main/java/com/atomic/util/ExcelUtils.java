package com.atomic.util;

import com.atomic.param.Constants;
import com.atomic.param.TestNGUtils;
import com.atomic.param.entity.MethodMeta;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.ITestResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public class ExcelUtils {

    private int totalRows = 0;
    private int totalCells = 0;
    private String errorInfo;
    private int sheetIndex = 0;

    private static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    private static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

    private static List<Map<String, String>> reflectMapList(List<List<String>> list) {
        ExcelUtils poi = new ExcelUtils();
        List<Map<String, String>> mList = Lists.newArrayList();
        Map<String, String> map;
        if (list != null) {
            for (int i = 1; i < list.size(); i++) {
                map = Maps.newHashMap();
                List<String> cellList = list.get(i);
                for (int j = 0; j < cellList.size(); j++) {
                    map.put((String) ((List) list.get(0)).get(j), cellList.get(j));
                }
                mList.add(map);
            }
        }
        return mList;
    }

    /**
     * 所有属性都是空串才不算测试用例
     * @param lists
     * @param i
     * @param listTitleSize
     * @return
     */
    private static boolean isEmptyTestCase(List<List<String>> lists, int i, int listTitleSize) {
        for (int k = 0; k < listTitleSize; k++) {
            if (!StringUtils.isEmpty(lists.get(k).get(i))) {
                return false;
            }
        }
        return true;
    }

    public static List<Map<String, Object>> generateCasesFromCsv(List<String[]> dataList) {
        List<List<String>> lists = Lists.newArrayList();
        String bom = "\uFEFF";
        for (int i = 0; i < dataList.size(); i++) {
            // 处理首字符BOM问题
            if (i == 0 && dataList.get(i)[0].startsWith(bom)) {
                dataList.get(i)[0] = dataList.get(i)[0].replace(bom, "");
            }
            lists.add(Arrays.asList(dataList.get(i)));
        }
        return generateCases(lists);
    }

    public static List<Map<String, Object>> generateCases(List<List<String>> lists) {
        List<Map<String, Object>> datas = Lists.newArrayList();
        List<String> listTitle = Lists.newArrayList();
        // 获取标题
        for (int j = 0; j < lists.size(); j++) {
            listTitle.add(lists.get(j).get(0));
        }
        // 获取数据
        for (int i = 1; i < lists.get(0).size(); i++) {
            Map<String, Object> dataMap = Maps.newHashMap();
            // if (!"".equals(lists.get(0).get(i))) {
            // 所有属性都是空串才不算测试用例
            if (!isEmptyTestCase(lists, i, listTitle.size())) {
                for (int k = 0; k < listTitle.size(); k++) {
                    if (listTitle.get(k) != null) {
                        dataMap.put(listTitle.get(k), lists.get(k).get(i));
                    }
                }
                dataMap.put(Constants.CASE_INDEX, i);
                datas.add(dataMap);
            }
        }
        return datas;
    }

    private int getTotalRows() {
        return this.totalRows;
    }

    private int getTotalCells() {
        return this.totalCells;
    }

    private String getErrorInfo() {
        return this.errorInfo;
    }

    public List<List<String>> read(String filePath, String sheetName) {
        List<List<String>> dataLst = Lists.newArrayList();
        InputStream is = null;
        if (!validateExcel(filePath)) {
            System.out.println(this.errorInfo);
            return null;
        }
        boolean isExcel2003 = true;
        if (isExcel2007(filePath)) {
            isExcel2003 = false;
        }
        try {
            File file = new File(filePath);
            is = new FileInputStream(file);
            Workbook wb;
            if (isExcel2003) {
                wb = new HSSFWorkbook(is);
            } else {
                wb = new XSSFWorkbook(is);
            }
            dataLst = read(wb, sheetName);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            is = getInputStream(is);
        } finally {
            getInputStream(is);
        }
        return dataLst;
    }

    public List<List<String>> read(InputStream inputStream, boolean isExcel2003) {
        List<List<String>> dataLst = null;
        try {
            Workbook wb;
            if (isExcel2003) {
                wb = new HSSFWorkbook(inputStream);
            } else {
                wb = new XSSFWorkbook(inputStream);
            }
            dataLst = read(wb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataLst;
    }

    private boolean validateExcel(String filePath) {
        if ((filePath == null) || ((!isExcel2003(filePath)) && (!isExcel2007(filePath)))) {
            this.errorInfo = "文件名不是excel格式";
            return false;
        }
        // System.out.println(filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            this.errorInfo = "文件不存在";
            return false;
        }
        return true;
    }

    private InputStream getInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e1) {
                is = null;

                e1.printStackTrace();
            }
        }
        return is;
    }

    private List<List<String>> read(Workbook wb, int sheetIndex) {
        List<List<String>> dataLst = Lists.newArrayList();
        Sheet sheet = wb.getSheetAt(sheetIndex);
        this.totalRows = sheet.getPhysicalNumberOfRows();
        if ((this.totalRows >= 1) && (sheet.getRow(0) != null)) {
            // 是获取不为空的列个数
            // this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            // 获取最后一个不为空的列是第几个
            this.totalCells = sheet.getRow(0).getLastCellNum();
        }
        for (int r = 0; r < this.totalRows; r++) {
            Row row = sheet.getRow(r);
            if (row != null) {
                List<String> rowLst = Lists.newArrayList();
                for (int c = 0; c < getTotalCells(); c++) {
                    Cell cell = row.getCell(c);
                    String cellValue = "";
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case 0:
                                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                    Date date = cell.getDateCellValue();
                                    cellValue = dateToStr(date, "yyyy-MM-dd HH:mm:ss");
                                } else {
                                    Integer num = (int) cell.getNumericCellValue();
                                    cellValue = String.valueOf(num);
                                }
                                break;
                            case 1:
                                cellValue = cell.getStringCellValue().trim();
                                break;
                            case 4:
                                cellValue = String.valueOf(cell.getBooleanCellValue());
                                break;
                            case 2:
                                cellValue = cell.getCellFormula();
                                break;
                            case 3:
                                cellValue = "";
                                break;
                            case 5:
                                cellValue = "非法字符";
                                break;
                            default:
                                cellValue = "未知类型";
                        }
                    }
                    rowLst.add(cellValue);
                }
                dataLst.add(rowLst);
            }
        }
        return dataLst;
    }

    private String dateToStr(Date date, String pattern) {
        return dateToStr(date, pattern, Locale.CHINA);
    }

    private String dateToStr(Date date, String pattern, Locale locale) {
        if (pattern == null) {
            pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        }
        DateFormat ymdhmsFormat = new SimpleDateFormat(pattern, locale);

        return ymdhmsFormat.format(date);
    }

    private List<List<String>> read(Workbook wb, String sheetName) {
        int sheetIndex = 0;
        try {
            sheetIndex = wb.getSheetIndex(sheetName);
        } catch (Exception ignored) {
        }
        if (sheetIndex < 0) {
            sheetIndex = 0;
        }
        return read(wb, sheetIndex);
    }

    private List<List<String>> read(Workbook wb) {
        return read(wb, 0);
    }

    public List<Map<String, String>> excelDatas(String filePath, String sheetName) {
        List<List<String>> lists = read(filePath, sheetName);
        return reflectMapList(lists);
    }

    /**
     * 按竖行方式读取数据
     * @param filePath
     * @param sheetName
     * @return
     */
    public List<Map<String, Object>> readDataByRow(String filePath, String sheetName) {
        List<Map<String, Object>> datas = Lists.newArrayList();
        List<List<String>> lists = read(filePath, sheetName);
        List<String> listTitel = Lists.newArrayList();
        // 获取标题
        for (int j = 0; j < lists.size(); j++) {
            listTitel.add(lists.get(j).get(0));
        }
        // 获取数据
        for (int i = 1; i < lists.get(0).size(); i++) {
            Map<String, Object> dataMap = Maps.newHashMap();
            // if (!"".equals(lists.get(0).get(i))) {
            // 所有属性都是空串才不算测试用例
            if (!isEmptyTestCase(lists, i, listTitel.size())) {
                for (int k = 0; k < listTitel.size(); k++) {
                    dataMap.put(listTitel.get(k), lists.get(k).get(i));
                }
                dataMap.put(Constants.CASE_INDEX, i);
                datas.add(dataMap);
            }
        }
        return datas;
    }

    /**
     * 读取对应的excel sheet页数据
     * @param testResult 测试上下文
     * @param instance   测试类实列
     * @param sheetName  excel sheet页名称
     * @return excel sheet页数据
     */
    public List<Map<String, Object>> readDataByRow(ITestResult testResult, Object instance, String sheetName) {
        String className = TestNGUtils.getTestCaseClassName(testResult);
        String resource = instance.getClass().getResource("").getPath();
        String xls = resource + className + ".xls";
        ExcelUtils excelUtil = new ExcelUtils();
        return excelUtil.readDataByRow(xls, sheetName);
    }

    /**
     * 读取对应的excel sheet页数据
     * @param methodMeta
     * @param sheetName excel sheet页名称
     * @return
     */
    public List<Map<String, Object>> readDataByRow(MethodMeta methodMeta, String sheetName) {
        Class testClass = methodMeta.getTestClass();
        String className = testClass.getSimpleName();
        String resource = testClass.getResource("").getPath();
        String xls = resource + className + ".xls";
        return readDataByRow(xls, sheetName);
    }

    public Map<String, Object> readDataByRow(ITestResult testResult, String sheetName,int index) {
        try {
            String className = TestNGUtils.getTestCaseClassName(testResult);
            Class<? extends Class> clazz = testResult.getTestClass().getRealClass().getClass();
            String resource = clazz.getResource("").getPath();
            String xls = resource + className + ".xls";
            ExcelUtils excelUtil = new ExcelUtils();
            List<Map<String, Object>> maps = excelUtil.readDataByRow(xls, sheetName);
            return maps.get(index - 1);
        } catch (Exception e) {
            return Maps.newHashMap();
        }
    }
}
