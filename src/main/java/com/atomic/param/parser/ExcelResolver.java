package com.atomic.param.parser;

import com.atomic.param.Constants;
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
import org.springframework.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
@ThreadSafe
public class ExcelResolver {

    /**
     * 错误信息
     */
    private volatile String errorInfo;
    /**
     * excel文件路径
     */
    private volatile String filePath;
    /**
     * excel sheet页名称
     */
    private volatile String sheetName;

    public ExcelResolver() {

    }

    public ExcelResolver(String filePath, String sheetName) {
        this.filePath = filePath;
        this.sheetName = sheetName;
    }

    /**
     * 按竖行方式读取数据
     * @return
     */
    public List<Map<String, Object>> readDataByRow() {
        Assert.notNull(filePath, "Excel 文件地址不能为空.");
        Assert.notNull(sheetName, "Excel sheet页名称不能为空.");
        List<Map<String, Object>> datas = Lists.newArrayList();
        List<List<String>> lists = read();
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
            if (Boolean.FALSE.equals(isEmptyTestCase(lists, i, listTitel.size()))) {
                for (int k = 0; k < listTitel.size(); k++) {
                    dataMap.put(listTitel.get(k), lists.get(k).get(i));
                }
                dataMap.put(Constants.CASE_INDEX, i);
                datas.add(dataMap);
            }
        }
        return datas;
    }

    public Map<String, Object> readDataByRow(int index) {
        try {
            List<Map<String, Object>> maps = readDataByRow();
            return maps.get(index - 1);
        } catch (Exception e) {
            return Maps.newHashMap();
        }
    }

    public Map<String, Object> readDataByRow(String filePath, String sheetName, int index) {
        this.filePath = filePath;
        this.sheetName = sheetName;
        return readDataByRow(index);
    }

    public List<Map<String, String>> excelDatas() {
        List<List<String>> lists = read();
        return reflectMapList(lists);
    }

    private List<Map<String, String>> reflectMapList(List<List<String>> list) {
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
    private boolean isEmptyTestCase(List<List<String>> lists, int i, int listTitleSize) {
        for (int k = 0; k < listTitleSize; k++) {
            if (!StringUtils.isEmpty(lists.get(k).get(i))) {
                return false;
            }
        }
        return true;
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

    private List<List<String>> read(Workbook wb, int sheetIndex) {
        List<List<String>> dataLst = Lists.newArrayList();
        Sheet sheet = wb.getSheetAt(sheetIndex);
        int totalRows = sheet.getPhysicalNumberOfRows();
        short cellNum = 0;
        if ((totalRows >= 1) && (sheet.getRow(0) != null)) {
            // 是获取不为空的列个数
            // this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            // 获取最后一个不为空的列是第几个
            cellNum = sheet.getRow(0).getLastCellNum();
        }
        for (int r = 0; r < totalRows; r++) {
            Row row = sheet.getRow(r);
            if (row != null) {
                List<String> rowLst = Lists.newArrayList();
                for (int c = 0; c < cellNum; c++) {
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

    private List<List<String>> read() {
        List<List<String>> dataLst = Lists.newArrayList();
        InputStream is = null;
        if (!validateExcel()) {
            System.out.println(this.errorInfo);
            return Lists.newArrayList();
        }
        boolean isExcel2003 = true;
        if (isExcel2007()) {
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

    private List<List<String>> read(InputStream inputStream, boolean isExcel2003) {
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

    private boolean validateExcel() {
        if ((filePath == null) || ((!isExcel2003()) && (!isExcel2007()))) {
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

    private boolean isExcel2003() {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    private boolean isExcel2007() {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
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

    private String dateToStr(Date date, String pattern) {
        if (pattern == null) {
            pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        }
        DateFormat ymdhmsFormat = new SimpleDateFormat(pattern, Locale.CHINA);

        return ymdhmsFormat.format(date);
    }
}
