package com.atomic.tools.assertcheck;

import com.alibaba.fastjson.JSON;
import com.atomic.param.Constants;
import com.atomic.tools.assertcheck.entity.AssertItem;
import com.atomic.tools.assertcheck.entity.SqlEntity;
import com.atomic.tools.assertcheck.enums.AssertType;
import com.atomic.tools.assertcheck.enums.CompareType;
import com.atomic.util.DataSourceUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 根据配置自动断言
 */
public final class AssertCheckUtils {

    private AssertCheckUtils() {

    }

    public static void assertCheck(Object testInstance, Map<String, Object> param) throws Exception {
        System.out.println("==================开始自动断言==================");
        List<AssertItem> assertItemList = getAssertItemList(testInstance, false);
        if (CollectionUtils.isEmpty(assertItemList)) {
            return;
        }
        for (int i = 0; i < assertItemList.size(); i++) {
            assertCheck(assertItemList.get(i), param, i);
        }
        System.out.println("==================自动断言完成==================");
        System.out.println();
    }

    public static List<AssertItem> getAssertItemList(Object testInstance, boolean isBeforeTest) throws IOException {
        // 先使用文件来代替
        String className = testInstance.getClass().getSimpleName();
        String resource = testInstance.getClass().getResource("").getPath();
        String path = resource + className + ".assert";
        File file = new File(path);
        if (!file.exists()) {
            if (!isBeforeTest) {
                System.out.println(String.format("没有找到对应的断言配置文件, path is %s", path));
            }
            return null;
        }
        String json = Files.asCharSource(file, Charsets.UTF_8).read();
        if (org.apache.commons.lang3.StringUtils.isEmpty(json)) {
            System.out.println("断言配置文件没有内容");
            return null;
        }
        List<AssertItem> assertItemList = JSON.parseArray(json, AssertItem.class);
        if (CollectionUtils.isEmpty(assertItemList)) {
            throw new RuntimeException(String.format("转化断言失败:%s", json));
        }
        return assertItemList;
    }

    private static void assertCheck(AssertItem item, Map<String, Object> param, int index) throws Exception {
        Object oldValue;
        String oldSql = null;
        String newSql = null;
        // 断言前就从数据库获取值
        if (item.getAssertType() == AssertType.OLD_VALUE_BEFORE_TEST.getCode()) {
            oldValue = param.get(Constants.ASSERT_ITEM_ + index);
            oldSql = param.get(Constants.OLD_SQL_ + index) == null ? null :
                    String.valueOf(param.get(Constants.OLD_SQL_ + index));
        } else {
            oldValue = item.getOldValue();
            if (org.apache.commons.lang3.StringUtils.isEmpty(item.getOldValue())) {
                oldValue = getSqlValue(item.getOldSqlEntity(), param);
                oldSql = item.getOldSqlEntity().getSql();
            }
        }
        Object newValue = item.getNewValue();
        // 断言是今天不需要值2
        if (item.getCompareType() != CompareType.TODAY.getCode()) {
            if (org.apache.commons.lang3.StringUtils.isEmpty(item.getNewValue())) {
                newValue = getSqlValue(item.getNewSqlEntity(), param);
                newSql = item.getNewSqlEntity().getSql();
            } else {
                newValue = stringFormat(newValue.toString(), param); // 支持{key}占位符
            }
        }
        if (oldValue == null) {
            throw new RuntimeException("断言缺少值1");
        }
        // 断言是今天不需要值2
        if (newValue == null && item.getCompareType() != CompareType.TODAY.getCode()) {
            throw new RuntimeException("断言缺少值2");
        }
        /*if ((oldValue instanceof List && !(newValue instanceof List)) || !(oldValue instanceof List) && newValue instanceof List) {
            throw new RuntimeException(String.format("断言值1与值2类型不匹配, oldValue is %s, newValue is %s", JSON.toJSONString(oldValue), JSON.toJSONString(newValue)));
		}*/
        compare(item.getCompareType(), oldValue, newValue, item.getFixedValue(), oldSql, newSql);
    }

    /**
     * 格式化字符串 字符串中使用{key}表示占位符
     * @param sourStr
     * @param param
     * @return
     */
    private static String stringFormat(String sourStr, Map<String, Object> param) throws SQLException {
        if (CollectionUtils.isEmpty(param))
            return sourStr;
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(sourStr);
        while (matcher.find()) {
            String key = matcher.group();
            String keyclone = key.substring(1, key.length() - 1).trim();
            Object value = param.get(keyclone);
            if (value != null) {
                // 入参也可能是 sql ，不用变，在获取sql的时候就重新赋值
                // value = getSqlValue(value.toString(), String.class);
                sourStr = sourStr.replace(key, value.toString());
            }
        }
        return sourStr;
    }

    @SuppressWarnings("unchecked")
    private static void compare(int compareType,
                                Object oldValue,
                                Object newValue,
                                int fixedValue,
                                String oldSql,
                                String newSql) throws ParseException {

        boolean isOldList = oldValue instanceof List;
        boolean isNewList = newValue instanceof List;
        List<Map<Integer, Object>> oldList = null;
        List<Map<Integer, Object>> newList = null;
        if (isOldList) {
            oldList = (List<Map<Integer, Object>>) oldValue;
        }
        if (isNewList) {
            newList = (List<Map<Integer, Object>>) newValue;
        }
        if (isOldList) {
            // 如果值2和值1都是列表，那就一一对应断言；如果值1是列表，值2就一个，那就循环值1列表进行断言
            if (isNewList) {
                // 元素个数相同
                Assert.assertEquals(oldList.size(), newList.size());
            }
            for (int i = 0; i < oldList.size(); i++) {
                Map<Integer, Object> oldMap = oldList.get(i);
                // map里面每个key的值进行断言
                for (int key : oldMap.keySet()) {
                    assertCheck(compareType, String.valueOf(oldMap.get(key)),
                            getNewValue(isNewList, newList, newValue, i, key), fixedValue, oldSql, newSql);
                }
            }
        } else {
            assertCheck(compareType, String.valueOf(oldValue), String.valueOf(newValue), fixedValue, oldSql, newSql);
        }
    }

    private static String getNewValue(boolean isNewList,
                                      List<Map<Integer, Object>> newList,
                                      Object newValue,
                                      int index,
                                      int key) {

        if (isNewList) {
            return String.valueOf(newList.get(index).get(key));
        } else {
            return String.valueOf(newValue);
        }
    }

    private static void assertCheck(int compareType,
                                    String oldValue,
                                    String newValue,
                                    int fixedValue,
                                    String oldSql,
                                    String newSql) throws ParseException {

        CompareType compare = CompareType.getCompareType(compareType);
        if (compare != null) {
            switch (compare) {
                case EQUALS:// 等于
                    print(String.format("值2（%s）= 值1（%s）", newValue, oldValue), oldSql, newSql);
                    Assert.assertEquals(oldValue, newValue);
                    break;
                case NOT_EQUALS:// 不等于
                    print(String.format("值2（%s）!= 值1（%s）", newValue, oldValue), oldSql, newSql);
                    Assert.assertNotEquals(oldValue, newValue);
                    break;
                case GREATER_THAN:// 值2大于值1
                    print(String.format("值2（%s）> 值1（%s）", newValue, oldValue), oldSql, newSql);
                    Assert.assertTrue(Double.valueOf(newValue).compareTo(Double.valueOf(oldValue)) > 0);
                    break;
                case GREATER_THAN_AND_EQUALS:// 值2大于等于值1
                    print(String.format("值2（%s）>= 值1（%s）", newValue, oldValue), oldSql, newSql);
                    Assert.assertTrue(Double.valueOf(newValue).compareTo(Double.valueOf(oldValue)) >= 0);
                    break;
                case LESS_THAN:// 值2小于值1
                    print(String.format("值2（%s）< 值1（%s）", newValue, oldValue), oldSql, newSql);
                    Assert.assertTrue(Double.valueOf(newValue).compareTo(Double.valueOf(oldValue)) < 0);
                    break;
                case LESS_THAN_AND_EQUALS:// 值2小于等于值1
                    print(String.format("值2（%s）<= 值1（%s）", newValue, oldValue), oldSql, newSql);
                    Assert.assertTrue(Double.valueOf(newValue).compareTo(Double.valueOf(oldValue)) <= 0);
                    break;
                case GREATER_THAN_VALUE:// 值2比值1大多少
                    print(String.format("值2（%s）- 值1（%s）= %s", newValue, oldValue, fixedValue), oldSql, newSql);
                    Assert.assertEquals(Integer.valueOf(newValue).intValue(),
                            Integer.parseInt(oldValue) + fixedValue);
                    break;
                case LESS_THAN_VALUE:// 值2比值1小多少
                    print(String.format("值1（%s）- 值2（%s）= %s", newValue, oldValue, fixedValue), oldSql, newSql);
                    Assert.assertEquals(Integer.valueOf(newValue).intValue(),
                            Integer.parseInt(oldValue) - fixedValue);
                    break;
                case SAME_DAY:// 同一天
                    print(String.format("值2（%s）和 值1（%s）是同一天", newValue, oldValue, fixedValue), oldSql, newSql);
                    // 暂时只支持 yyyy-MM-dd 和 时间戳
                    Assert.assertTrue(isSameDay(strToDate(oldValue), strToDate(newValue)));
                    break;
                case TODAY:// 今天
                    print(String.format("值1（%s）是今天", oldValue, fixedValue), oldSql, newSql);
                    // 暂时只支持 yyyy-MM-dd 和 时间戳
                    Assert.assertTrue(isSameDay(strToDate(oldValue), new Date()));
                    break;
                case SAME_DAY_UNIX_TIME:// 时间戳是同一天
                    print(String.format("值2（%s）和 值1（%s）是同一天", newValue, oldValue, fixedValue), oldSql, newSql);
                    Assert.assertTrue(isSameDay(Integer.parseInt(newValue), Integer.parseInt(oldValue)));
                    break;
                case TODAY_UNIX_TIME:// 时间戳是今天
                    print(String.format("值1（%s）是今天", oldValue, fixedValue), oldSql, newSql);
                    Assert.assertTrue(isSameDay(Integer.parseInt(oldValue), new Date()));
                    break;
                default:
                    break;
            }
        }
    }

    private static Date strToDate(String str) throws ParseException {
        if (NumberUtils.isNumber(str)) {
            return new Date(Integer.parseInt(str) * 1000L);
        } else {
            return strToDate(str, "yyyy-MM-dd");
        }
    }

    private static Date strToDate(String str, String pattern) throws ParseException {
        return strToDate(str, pattern, Locale.CHINA);
    }

    private static Date strToDate(String str, String pattern, Locale locale)
            throws ParseException {
        if (pattern == null) {
            pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        }
        DateFormat ymdhmsFormat = new SimpleDateFormat(pattern, locale);
        return ymdhmsFormat.parse(str);
    }

    private static boolean isSameDay(Date day1, Date day2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ds1 = sdf.format(day1);
        String ds2 = sdf.format(day2);
        if (ds1.equals(ds2)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isSameDay(int unixTime, Date day) {
        return isSameDay(new Date(unixTime * 1000L), day);
    }

    private static boolean isSameDay(int unixTime1, int unixTime2) {
        return isSameDay(new Date(unixTime1 * 1000L), new Date(unixTime2 * 1000L));
    }

    private static void print(String msg, String oldSql, String newSql) {
        System.out.println(String.format("断言：%s", msg));
        if (!org.apache.commons.lang3.StringUtils.isEmpty(oldSql)) {
            System.out.println(String.format("值1的Sql：%s", oldSql));
        }
        if (!org.apache.commons.lang3.StringUtils.isEmpty(newSql)) {
            System.out.println(String.format("值2的Sql：%s", newSql));
        }
    }

    public static List<Map<Integer, Object>> getSqlValue(SqlEntity sqlEntity,
                                                          Map<String, Object> context) throws Exception {

        String sql = sqlEntity.getSql();
        if (org.apache.commons.lang3.StringUtils.isEmpty(sql)) {
            if (org.apache.commons.lang3.StringUtils.isEmpty(sqlEntity.getCondition())) {
                sql = String.format("select %s from %s where 1=1", sqlEntity.getFields(), sqlEntity.getTable());
            } else {
                sql = String.format("select %s from %s where 1=1 and %s", sqlEntity.getFields(),
                        sqlEntity.getTable(), sqlEntity.getCondition());
            }
        }
        // sql语句 增加 {key} 占位符功能
        sql = stringFormat(sql, context);
        sqlEntity.setSql(sql);// 给sql赋值，外面获取
        return DataSourceUtils.queryData(sqlEntity.getDataSource(), sql);
    }

    public static void main(String[] args) {
        AssertItem item = new AssertItem();
        item.setCompareType(CompareType.GREATER_THAN_VALUE.getCode());
        item.setFixedValue(2);
        item.setAssertType(AssertType.OLD_VALUE_BEFORE_TEST.getCode());
        SqlEntity oldSqlEntity = new SqlEntity();
        oldSqlEntity.setDataSource("memberMasterDataSource");
        oldSqlEntity.setTable("mb_user_category_logall");
        oldSqlEntity.setFields("log_id");
        oldSqlEntity.setCondition("user_id=18");
        item.setOldSqlEntity(oldSqlEntity);

        SqlEntity newSqlEntity = new SqlEntity();
        newSqlEntity.setDataSource("memberMasterDataSource");
        newSqlEntity.setTable("mb_user_category_logall");
        newSqlEntity.setFields("log_id");
        newSqlEntity.setCondition("user_id=14035803");
        item.setNewSqlEntity(newSqlEntity);

        System.out.println(JSON.toJSONString(item));

        oldSqlEntity.setFields("date");
        item.setNewValue("2013-09-04");
        item.setCompareType(CompareType.SAME_DAY.getCode());
        item.setNewSqlEntity(null);
        item.setFixedValue(0);
        System.out.println(JSON.toJSONString(item));

    }
}
