package com.atomic.param;


import com.atomic.annotations.AnnotationUtils;
import com.atomic.enums.AutoTestEnum;
import com.atomic.enums.AutoTestMode;
import com.atomic.exception.AutoTestException;
import com.atomic.param.entity.MethodMeta;
import com.atomic.param.values.AutoTestBigDecimalValues;
import com.atomic.param.values.AutoTestBooleanValues;
import com.atomic.param.values.AutoTestByteValues;
import com.atomic.param.values.AutoTestDateValues;
import com.atomic.param.values.AutoTestDoubleValues;
import com.atomic.param.values.AutoTestFloatValues;
import com.atomic.param.values.AutoTestIntegerValues;
import com.atomic.param.values.AutoTestLongValues;
import com.atomic.param.values.AutoTestShortValues;
import com.atomic.param.values.AutoTestStringValues;
import com.atomic.param.values.IAutoTestValues;
import com.atomic.util.ListUtils;
import com.atomic.util.ReflectionUtils;
import com.g7.framework.common.dto.BaseRequest;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.atomic.annotations.AnnotationUtils.getAutoTestMode;
import static com.atomic.exception.ThrowException.throwNewException;
import static com.atomic.param.Constants.DATE_FORMAT;
import static com.atomic.param.MethodMetaUtils.getMethodMeta;
import static com.atomic.param.ParamUtils.getParamContextWithoutExtraInfo;
import static com.atomic.param.ParamUtils.getParamName;
import static com.atomic.param.StringUtils.getValue;
import static com.atomic.param.StringUtils.isBasicType;
import static java.util.Comparator.comparing;

/**
 * @author dreamyao
 */
public class AutoTest {

    public static void handleException(Exception e,
                                       Map<String, List<Map<String, Object>>> exceptionMsgs,
                                       ITestResult testResult,
                                       Map<String, Object> param) {

        String methodName = testResult.getTestClass().getRealClass().getName();
        Map<String, Object> newParam = getParamContextWithoutExtraInfo(param);
        String error = String.format("方法执行自动化测试时发生错误！, 方法名是：%s, 入参是：%s",
                methodName, getJSONStringWithDateFormat(newParam));
        Reporter.log("[AutoTest#handleException()]:{" + error + "}");

        String errorMsg = e.getMessage() != null ? e.getMessage() :
                ((InvocationTargetException) e).getTargetException() != null ?
                        (((InvocationTargetException) e).getTargetException()).getMessage() : null;

        errorMsg = "(" + methodName + ")" + errorMsg;
        List<Map<String, Object>> exceptions = exceptionMsgs.get(errorMsg);
        if (exceptions == null) {
            exceptions = Lists.newArrayList();
        }
        if (!exceptionMsgs.containsKey(errorMsg)) {
            exceptionMsgs.put(errorMsg, exceptions);
        }
        exceptions.add(newParam);
    }

    public static void printExceptions(Exception exception,
                                       Map<String, List<Map<String, Object>>> exceptionMsgs) throws Exception {
        if (exception != null) {
            System.out.println("-----------------------下面打印所有异常信息：" +
                    "-----------------------------------------------");
            // 打印所有遇到的异常
            for (String msg : exceptionMsgs.keySet()) {
                System.out.println("-----------------------------------我是分割线-----------------------------------");
                System.err.println("异常：" + msg);
                System.err.println("测试用例列表：" + getJSONString(exceptionMsgs.get(msg)));
            }
            System.out.println("---------------------- -异常信息打印完毕，下面抛出异常 " +
                    "-----------------------------------------------");
            //先判断异常具体类型
            throwNewException(exception);
            // 抛出异常，提醒测试未通过
            throw exception;
        }
    }

    /**
     * 对象转json
     * @param obj
     * @return
     */
    private static String getJSONStringWithDateFormat(Object obj) {
        return ParamUtils.getJSONStringWithDateFormat(obj, true, DATE_FORMAT);
    }

    /**
     * 对象转json
     * @param obj
     * @return
     */
    private static String getJSONString(Object obj) {
        return getJSONString(obj, true);
    }

    /**
     * 对象转json
     * @param obj          对象
     * @param prettyFormat 是否格式化Json字符串
     * @return 格式化后的Json字符串
     */
    private static String getJSONString(Object obj, boolean prettyFormat) {
        return ParamUtils.getJSONStringWithDateFormat(obj, prettyFormat, null);
    }

    /**
     * 生成自动化测试所需的数据
     * @param testResult
     * @return
     */
    public static List<Map<String, Object>> generateAutoTestCases(ITestResult testResult,
                                                                  Object testInstance) throws Exception {

        Map<String, Object> param = TestNGUtils.getParamContext(testResult);
        MethodMeta methodMeta = getMethodMeta(param, testResult, testInstance);
        Type[] parameterTypes = methodMeta.getParamTypes();
        List<AutoTest.AutoTestItem> autoTestItemList = Lists.newArrayList();
        generateAutoTestCases(methodMeta, parameterTypes, autoTestItemList);
        // 去重
        autoTestItemList = ListUtils.distinct(autoTestItemList, comparing(AutoTestItem::getParamName));
        // 多属性模式，属性之间用组合模式；单属性模式，每次一个属性变化，其他属性使用正常值
        if (getAutoTestMode(TestNGUtils.getTestMethod(testResult)) == AutoTestMode.MULTIPLE) {
            return generateAutoTestCasesByMultiple(param, autoTestItemList, TestNGUtils.getTestMethod(testResult));
        } else {
            return generateAutoTestCasesBySingle(param, autoTestItemList);
        }
    }

    /**
     * 生成自动化测试所需的数据
     * @param testResult
     * @return
     */
    public static List<Map<String, Object>> generateAutoTestCases(ITestResult testResult,
                                                                  Class<?> interfaceType,
                                                                  String testMethodName,
                                                                  CompletableFuture<Object> future) throws Exception {

        Map<String, Object> param = TestNGUtils.getParamContext(testResult);
        MethodMeta methodMeta = getMethodMeta(testResult, interfaceType, testMethodName, param, future);
        Type[] parameterTypes = methodMeta.getParamTypes();
        List<AutoTestItem> autoTestItemList = Lists.newArrayList();
        generateAutoTestCases(methodMeta, parameterTypes, autoTestItemList);
        // 去重
        autoTestItemList = ListUtils.distinct(autoTestItemList, comparing(AutoTestItem::getParamName));
        // 多属性模式，属性之间用组合模式；单属性模式，每次一个属性变化，其他属性使用正常值
        if (getAutoTestMode(TestNGUtils.getTestMethod(testResult)) == AutoTestMode.MULTIPLE) {
            return generateAutoTestCasesByMultiple(param, autoTestItemList, TestNGUtils.getTestMethod(testResult));
        } else {
            return generateAutoTestCasesBySingle(param, autoTestItemList);
        }
    }

    private static void generateAutoTestCases(MethodMeta methodMeta,
                                              Type[] parameterTypes,
                                              List<AutoTest.AutoTestItem> autoTestItemList) throws Exception {

        // 正常个数的测试用例
        int autoTestValuesLevel = AutoTestEnum.NORMAL.getLevel();
        for (int i = 0; i < parameterTypes.length; i++) {
            // 实体类，且不是包装类
            if (methodMeta.getParamTypes()[i] instanceof Class) {
                String simpleClassName = ((Class<?>) methodMeta.getParamTypes()[i]).getSimpleName();
                if (!isBasicType(simpleClassName)) {
                    // 获取该类的所有属性是基本类型的可能值
                    autoTestItemList.addAll(AutoTest.getAutoTestValues(((Class<?>) methodMeta.getParamTypes()[i]),
                            autoTestValuesLevel));
                } else {
                    // 单参数
                    autoTestItemList.add(AutoTest.getAutoTestValues((Class<?>) methodMeta.getParamTypes()[i],
                            methodMeta.getParamNames()[i], autoTestValuesLevel));
                }
            } else {
                // XXX<T> 中的 T
                Type paramType = getParamType(methodMeta.getDeclaredInterfaceMethod(), i);
                // Request型
                if (ParamUtils.isParamTypeExtendsBaseRequest(methodMeta.getDeclaredInterfaceMethod(), i)) {
                    Class<?> paramClass = (Class<?>) paramType;
                    if (BaseRequest.class.isAssignableFrom(paramClass)) {
                        // 获取该类的所有属性是基本类型的可能值
                        autoTestItemList.addAll(AutoTest.getAutoTestValues(paramClass.asSubclass(BaseRequest.class),
                                autoTestValuesLevel));
                    } else {
                        generateAutoTestParamClass(methodMeta, autoTestItemList, autoTestValuesLevel, i, paramClass);
                    }
                } else {
                    // 其他泛型

                    // 先自动生成公共字段的值
                    autoTestItemList.addAll(getAutoTestCommonValues((Class<?>) paramType, autoTestValuesLevel));
                    // 在字段生成 T 的值
                    Class<?> paramClass = (Class<?>) paramType;
                    generateAutoTestParamClass(methodMeta, autoTestItemList, autoTestValuesLevel, i, paramClass);

                }
            }
        }
    }

    /**
     * 生成XXX<T> 中 T 参数的值
     * @param methodMeta          测试属性
     * @param autoTestItemList    属性以及对应生成的值
     * @param autoTestValuesLevel 等级
     * @param i                   循环
     * @param paramClass          T 的 Class 类型
     * @throws ClassNotFoundException Class不存在
     */
    private static void generateAutoTestParamClass(MethodMeta methodMeta,
                                                   List<AutoTestItem> autoTestItemList,
                                                   int autoTestValuesLevel, int i,
                                                   Class<?> paramClass) throws ClassNotFoundException {

        if (Number.class.isAssignableFrom(paramClass)) {
            // Request<? extend Number>
            // 单参数
            autoTestItemList.add(getAutoTestValues(paramClass.asSubclass(Number.class),
                    getParamName(methodMeta, i), autoTestValuesLevel));
        } else if (String.class.isAssignableFrom(paramClass)) {
            // Request<String>
            // 单参数
            autoTestItemList.add(getAutoTestValues(paramClass, getParamName(methodMeta, i), autoTestValuesLevel));
        } else {
            // Request<XXXDTO>
            // 获取该类的所有属性是基本类型的可能值
            autoTestItemList.addAll(getAutoTestValues(paramClass, autoTestValuesLevel));
        }
    }

    /**
     * 获取参数类型，如果是继承的 BaseRequest ，则获取泛型的实际类型
     * @param method     方法
     * @param paramIndex 参数序号
     * @return 参数类型
     * @throws Exception 异常
     */
    private static Type getParamType(Method method, int paramIndex) throws Exception {
        if (method.getGenericParameterTypes()[paramIndex] instanceof ParameterizedType) {
            if (ArrayUtils.isEmpty(((ParameterizedType) (method.getGenericParameterTypes()[paramIndex]))
                    .getActualTypeArguments())) {
                Reporter.log("[ParamUtils#getParamType()]:{} ---> 不能得到实际的类型参数！");
                throw new Exception("---------------不能得到实际的类型参数!---------------");
            }
            // Request<List<Integer>>型
            if (((ParameterizedType) (method.getGenericParameterTypes()[paramIndex]))
                    .getActualTypeArguments()[0] instanceof ParameterizedType) {
                return ((sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl)
                        ((ParameterizedType) (method.getGenericParameterTypes()[paramIndex]))
                                .getActualTypeArguments()[0]).getRawType();
            } else {
                // Request<XXX>型
                return ((ParameterizedType) (method.getGenericParameterTypes()[paramIndex]))
                        .getActualTypeArguments()[0];
            }
        }
        return method.getGenericParameterTypes()[paramIndex];
    }

    /**
     * 获取自动化测试值
     * @param paramClass
     * @param paramName
     * @return
     * @throws ClassNotFoundException
     */
    private static AutoTestItem getAutoTestValues(Class<?> paramClass,
                                                  String paramName,
                                                  int autoTestValuesLevel) throws ClassNotFoundException {
        AutoTestItem item = new AutoTestItem(paramName);
        item.setParamClass(paramClass);
        IAutoTestValues autoTestValues;
        switch (paramClass.getSimpleName()) {
            case "BigDecimal":
                autoTestValues = new AutoTestBigDecimalValues();
                break;
            case "Boolean":
            case "boolean":
                autoTestValues = new AutoTestBooleanValues();
                break;
            case "Byte":
            case "byte":
                autoTestValues = new AutoTestByteValues();
                break;
            case "Date":
                autoTestValues = new AutoTestDateValues();
                break;
            case "Double":
            case "double":
                autoTestValues = new AutoTestDoubleValues();
                break;
            case "Float":
            case "float":
                autoTestValues = new AutoTestFloatValues();
                break;
            case "Integer":
            case "int":
                autoTestValues = new AutoTestIntegerValues();
                break;
            case "Long":
            case "long":
                autoTestValues = new AutoTestLongValues();
                break;
            case "Short":
            case "short":
                autoTestValues = new AutoTestShortValues();
                break;
            case "String":
                autoTestValues = new AutoTestStringValues();
                break;
            default:
                throw new ClassNotFoundException(paramClass.getSimpleName());
        }
        item.setAutoTestValues(autoTestValues.getAutoTestValues(autoTestValuesLevel));
        return item;
    }

    /**
     * 获取自动化测试值
     * @param clazz
     * @param autoTestValuesLevel
     * @return
     * @throws ClassNotFoundException
     */
    private static List<AutoTestItem> getAutoTestValues(Class<?> clazz,
                                                        int autoTestValuesLevel) throws ClassNotFoundException {
        List<AutoTestItem> list = Lists.newArrayList();
        Optional<Field[]> optionalFields = Optional.of(clazz.getDeclaredFields());
        optionalFields.ifPresent(fields -> Arrays.stream(fields).forEach(
                newField -> generateNestingAutoTestValues(newField, list, autoTestValuesLevel)));
        return list;
    }

    /**
     * 递归的为对象嵌套对象的所有Field设值
     * @param field         field
     * @param autoTestItems 属性和生成的值
     */
    private static void generateNestingAutoTestValues(Field field,
                                                      List<AutoTestItem> autoTestItems,
                                                      int autoTestValuesLevel) {

        try {
            if (isBasicType(field.getType().getSimpleName())) {
                autoTestItems.add(getAutoTestValues(field.getType(), field.getName(), autoTestValuesLevel));
            } else {
                Type genericType = field.getGenericType();
                List<Field> fields = ReflectionUtils.getAllFieldsList((Class) genericType);
                for (Field newField : fields) {
                    generateNestingAutoTestValues(newField, autoTestItems, autoTestValuesLevel);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 对 XXX<T> 中的XXX 中的公共字段设值
     * @param clazz               入参对象Class
     * @param autoTestValuesLevel 等级
     * @return 属性和生成的值
     */
    private static List<AutoTestItem> getAutoTestCommonValues(Class<?> clazz, int autoTestValuesLevel) {
        List<AutoTestItem> list = Lists.newArrayList();
        List<Field> fields = ReflectionUtils.getAllFieldsList(clazz);
        // 必须要排除data 因为data为泛型，否则field.getGenericType()会报错
        List<Field> collect = fields.stream()
                .filter(field -> Boolean.FALSE.equals(field.getName().equals("serialVersionUID") ||
                        field.getName().equals("data")))
                .collect(Collectors.toList());

        for (Field field : collect) {
            generateNestingAutoTestValues(field, list, autoTestValuesLevel);
        }
        return list;
    }

    /**
     * 步入下一个循环
     * @param index
     * @param counts
     * @return 变化参数的下标
     */
    private static int increaseByMulti(int[] index, int[] counts) {
        for (int i = counts.length - 1; i >= 0; i--) {
            // 下标最大值
            if (index[i] < counts[i] - 1) {
                index[i]++;
                // 上层都置0
                for (int j = counts.length - 1; j > i; j--) {
                    index[j] = 0;
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * 步入下一个循环
     * @param index
     * @param counts
     * @return 变化参数的下标
     */
    private static int increaseBySingle(int[] index, int[] counts) {
        for (int i = counts.length - 1; i >= 0; i--) {
            // 下标最大值
            if (index[i] < counts[i] - 1) {
                index[i]++;
                return i;
            }
        }
        return -1;
    }

    /**
     * 计算自动测试实例总数
     * @param counts
     * @return
     */
    private static long getAutoTestCounts(int[] counts) {
        int allTests = 1;
        for (int i = counts.length - 1; i >= 0; i--) {
            allTests *= counts[i];
        }
        return allTests;
    }

    /**
     * 测试用例太多，内存不够用，减少实例
     * @param param
     * @param autoTestItemList
     * @param counts
     * @param autoTestValuesLevel
     * @throws ClassNotFoundException
     */
    private static void resetAutoTestValues(Map<String, Object> param,
                                            List<AutoTestItem> autoTestItemList,
                                            int[] counts,
                                            int autoTestValuesLevel) throws Exception {

        for (AutoTestItem item : autoTestItemList) {
            item.setAutoTestValues(getAutoTestValues(item.getParamClass(), item.getParamName(),
                    autoTestValuesLevel).getAutoTestValues());
        }
        // 把通过excel传入正常的入参值也加入到列表中去
        insertNormalTestValues(param, autoTestItemList);
        // 计算counts
        resetCounts(counts, autoTestItemList);
    }

    /**
     * 通过excel传入正常的入参值
     * @param param
     * @param autoTestItemList
     */
    @SuppressWarnings("unchecked")
    private static void insertNormalTestValues(Map<String, Object> param,
                                               List<AutoTestItem> autoTestItemList) throws Exception {

        for (AutoTestItem item : autoTestItemList) {
            String normalValue = getValue(param.get(item.getParamName()));
            if (!StringUtils.isExcelValueEmpty(normalValue)) {
                boolean alreadyContains = false;
                // 转成 String 来对比
                for (Object object : item.getAutoTestValues()) {
                    if (object != null && object.toString().equals(normalValue)) {
                        alreadyContains = true;
                        break;
                    }
                }
                if (!alreadyContains) {
                    // 这里直接添加String即可
                    item.getAutoTestValues().add(normalValue);
                }
            }
        }
    }

    private static void resetCounts(int[] counts, List<AutoTestItem> autoTestItemList) {
        for (int i = 0; i < counts.length; i++) {
            counts[i] = autoTestItemList.get(i).getAutoTestValues().size();
        }
    }

    /**
     * 遍历多维列表，生成测试用例，多属性模式
     * @param param
     * @param autoTestItemList
     * @return
     */
    private static List<Map<String, Object>> generateAutoTestCasesByMultiple(Map<String, Object> param,
                                                                             List<AutoTestItem> autoTestItemList,
                                                                             Method testMethod) throws Exception {

        int[] index = new int[autoTestItemList.size()];
        // 每个参数的自动测试值的个数
        // int[] counts = new int[autoTestItemList.size()];
        // 计算counts
        // resetCounts(counts, autoTestItemList);
        // 把通过excel传入正常的入参值也加入到列表中去
        insertNormalTestValues(param, autoTestItemList);
        // 计算counts
        int[] counts = autoTestItemList.stream().mapToInt(
                autoTestItem -> autoTestItem.getAutoTestValues().size()).toArray();
        long totalAutoTestCount = getAutoTestCounts(counts);
        // 如果测试用例太多，内存不够用，减少实例
        int maxTestCases = AnnotationUtils.getMaxTestCases(testMethod);
        int autoTestValuesLevel = AutoTestEnum.NORMAL.getLevel();
        while (totalAutoTestCount > maxTestCases || totalAutoTestCount < 0) {
            autoTestValuesLevel--;// 减少每个属性的测试值个数
            if (autoTestValuesLevel < AutoTestEnum.VERY_SMALL.getLevel()) {
                Reporter.log("[AutoTest#generateAutoTestCasesByMultiple()]:{自动化测试用例过多！}");
                throw new AutoTestException(String.format("auto test cases are too many to start, " +
                        "the max is %s, the combination number is %s, params is %s", maxTestCases,
                        totalAutoTestCount, getJSONString(autoTestItemList)));
            }
            // 减少各属性的测试值
            resetAutoTestValues(param, autoTestItemList, counts, autoTestValuesLevel);
            totalAutoTestCount = getAutoTestCounts(counts);
        }
        List<Map<String, Object>> list = new ArrayList<>((int) totalAutoTestCount);
        int changed;
        do {
            Map<String, Object> map = new HashMap<>();
            // 这里不用覆盖，在调用的地方再覆盖，节约内存
            for (int i = 0; i < index.length; i++) {
                map.put(autoTestItemList.get(i).getParamName(),
                        String.valueOf(autoTestItemList.get(i).getAutoTestValues().get(index[i])));
            }
            list.add(map);
            // 某维数组+1
            changed = increaseByMulti(index, counts);
        } while (changed >= 0);
        return list;
    }

    private static void setArray(int[] index, int value) {
        for (int i = 0; i < index.length; i++) {
            index[i] = value;
        }
    }

    /**
     * 遍历多维列表，生成测试用例，单属性模式，每次一个属性变化，其他属性使用正常值
     * @param param
     * @param autoTestItemList
     * @return
     */
    private static List<Map<String, Object>> generateAutoTestCasesBySingle(Map<String, Object> param,
                                                                           List<AutoTestItem> autoTestItemList)
            throws Exception {

        int[] index = new int[autoTestItemList.size()];
        // 设置默认-1
        setArray(index, -1);

        // 每个参数的自动测试值的个数
        // int[] counts = new int[autoTestItemList.size()];

        // 计算每个属性的测试值个数
        // resetCounts(counts, autoTestItemList);

        // 计算每个属性的测试值个数
        int[] counts = autoTestItemList.stream().mapToInt(
                autoTestItem -> autoTestItem.getAutoTestValues().size()).toArray();
        List<Map<String, Object>> list = new ArrayList<>();
        int changed = increaseBySingle(index, counts);
        while (changed >= 0) {
            // 这里直接覆盖
            Map<String, Object> newMap = new HashMap<>(param);
            // 把变化值覆盖进去
            newMap.put(autoTestItemList.get(changed).getParamName(),
                    String.valueOf(autoTestItemList.get(changed).getAutoTestValues().get(index[changed])));
            list.add(newMap);
            // 某维数组+1
            changed = increaseBySingle(index, counts);
        }
        return list;
    }

    public static class AutoTestItem {

        /**
         * 自动测试参数的名称
         */
        private String paramName;
        /**
         * 自动测试参数的类型
         */
        private Class paramClass;
        /**
         * 自动测试参数的自动测试列表
         */
        private List autoTestValues;

        public AutoTestItem(String paramName) {
            this.paramName = paramName;
        }

        public Class getParamClass() {
            return paramClass;
        }

        public void setParamClass(Class paramClass) {
            this.paramClass = paramClass;
        }

        public List getAutoTestValues() {
            return autoTestValues;
        }

        public void setAutoTestValues(List autoTestValues) {
            this.autoTestValues = autoTestValues;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }
    }
}
