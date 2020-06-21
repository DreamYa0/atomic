package com.atomic.util;

import mockit.Expectations;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * @author dreamyao
 * @title Mock工具类
 * @date 2018/6/7 下午10:00
 * @since 1.0.0
 */
public final class MockUtils {

    private static final Logger logger = LoggerFactory.getLogger(MockUtils.class);

    /**
     * 普通对象的方法mock
     * <pre>
     *     // mock SpringBean、mock普通类
     *     @Transactional
     *     @SpringBootTest(classes = WebApplication.class)
     *     public class TestUserSymbolAdd extends BaseTestCase<MemberUserController> {
     *
     *         @Autowired
     *         private MemberUserSymbolService memberUserSymbolService;
     *
     *         @Override
     *         public void beforeTest(Map<String, Object> context) {
     *             // mock Bean
     *             MockUtils.mock(memberUserSymbolService, "addUserSymbol", "mock返回值", ""mock入参");
     *
     *             // mock工具类
     *             MockUtils.mock(new MemberCheckUtil(), "getUserNo", "mock返回值", ""mock入参");
     *         }
     *
     *         @Test(dataProvider = Data.SINGLE,enabled = true)
     *         public void testCase(Map<String, Object> context, Object result) {
     *
     *         }
     *     }
     * </pre>
     * @param mockInstance mock对象实列
     * @param mockMethod   mock方法名称
     * @param mockReturn   mock返回结果
     * @param mockParamter mock入参
     */
    @SuppressWarnings("unchecked")
    public static void mock(Object mockInstance, String mockMethod, Object mockReturn, Object... mockParamter) {

        Assert.notNull(mockInstance, "Mock类实列不能为空");
        Assert.notNull(mockMethod, "Mock方法名称不能为空");

        Class<?> mockClass = mockInstance.getClass();
        // 为Class时
        try {

            Method method = ReflectionUtils.getMethod(mockClass, mockMethod);

            new Expectations(mockInstance) {
                {
                    // 非静态方法mock（包括普通方法、final方法）
                    MethodUtils.invokeMethod(mockInstance, mockMethod, mockParamter);
                    // 有返回值的方法
                    if (Boolean.FALSE.equals("void".equals(method.getReturnType().getName()))) {
                        result = mockReturn;
                    }
                }
            };

        } catch (Exception e) {
            logger.error("mock方法失败，mock方法名称为：{}", mockClass.getSimpleName() + "." + mockMethod, e);
        }
    }

    /**
     * mock 动态代理生成的类或接口，如mybatis的mapper
     * <pre>
     *     // mock 接口的方法，如mybatis的mapper
     *     @Transactional
     *     @SpringBootTest(classes = WebApplication.class)
     *     public class TestOrderCreate extends BaseTestCase<OrderManagerController> {
     *
     *         @Capturing
     *         private HttpSession session;
     *
     *         @Override
     *         public void beforeTest(Map<String, Object> context) {
     *             MockUtils.mock(session, "getAttribute", 1602578701956947969L, "userNo");
     *         }
     *
     *         @Test(dataProvider = Data.SINGLE,enabled = true)
     *         public void testCase(Map<String, Object> context, Object result) {
     *
     *         }
     *     }
     * </pre>
     * @param mockInstance mock对象实列
     * @param mockMethod   mock方法名称
     * @param mockReturn   mock返回结果
     * @param mockParamter mock入参
     */
    public static void mockProxy(Object mockInstance, String mockMethod, Object mockReturn, Object... mockParamter) {
        Assert.notNull(mockInstance, "Mock类实列不能为空");
        Assert.notNull(mockMethod, "Mock方法名称不能为空");

        // 如果mock的对象为接口时，如mybatis的mapper
        Class<?> mockClass = mockInstance.getClass();

        try {
            Method method = ReflectionUtils.getMethod(mockClass, mockMethod);
            new Expectations() {
                {
                    MethodUtils.invokeMethod(mockInstance, mockMethod, mockParamter);

                    // 有返回值的方法
                    if (Boolean.FALSE.equals("void".equals(method.getReturnType().getName()))) {
                        result = mockReturn;
                    }
                }
            };
        } catch (Exception e) {
            logger.error("mock方法失败，mock方法名称为：{}", mockClass.getSimpleName() + "." + mockMethod, e);
        }
    }

    /**
     * class对象mock
     * <pre>
     *     // mock工具类、mock无法实列化的类
     *     @Transactional
     *     @SpringBootTest(classes = WebApplication.class)
     *     public class TestUserSymbolAdd extends BaseTestCase<MemberUserController> {
     *
     *         @Override
     *         public void beforeTest(Map<String, Object> context) {
     *
     *             // mock工具类
     *             MockUtils.mock(MemberCheckUtil.class, "getUserNo", "mock返回值", ""mock入参");
     *         }
     *
     *         @Test(dataProvider = Data.SINGLE,enabled = true)
     *         public void testCase(Map<String, Object> context, Object result) {
     *
     *         }
     *     }
     * </pre>
     * @param mockClass mock class对象
     * @param mockMethod   mock方法名称
     * @param mockReturn   mock返回结果
     * @param mockParamter mock入参
     */
    public static void mockClass(Class<?> mockClass, String mockMethod, Object mockReturn, Object... mockParamter) {
        Assert.notNull(mockClass, "Mock类不能为空");
        Assert.notNull(mockMethod, "Mock方法名称不能为空");

        try {
            Method method = ReflectionUtils.getMethod(mockClass, mockMethod);
            // 静态方法mock
            new Expectations(mockClass) {
                {
                    MethodUtils.invokeStaticMethod(mockClass, mockMethod, mockParamter);

                    // 有返回值的方法
                    if (Boolean.FALSE.equals("void".equals(method.getReturnType().getName()))) {
                        result = mockReturn;
                    }
                }
            };
        } catch (Exception e) {
            logger.error("mock方法失败，mock方法名称为：{}", mockClass.getSimpleName() + "." + mockMethod, e);
        }
    }
}
