package com.atomic.tools.mock.listener;

import com.alibaba.fastjson.JSON;
import com.atomic.annotations.Mode;
import com.atomic.config.GlobalMode;
import com.atomic.config.GlobalModeHolder;
import com.atomic.config.TestMethodMode;
import com.atomic.tools.mock.data.MockContext;
import com.atomic.tools.mock.helper.MockFileHelper;
import com.google.common.io.Files;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.util.ReflectionUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
@ThreadSafe
public class SoaMockListener implements TestExecutionListener {

    private static final Method GET_APPLICATION_CONTEXT;

    static {
        try {
            GET_APPLICATION_CONTEXT = TestContext.class.getMethod("getApplicationContext");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private volatile GlobalModeHolder globalModeHolder;
    private volatile ApplicationContext applicationContext;

    private ApplicationContext getApplicationContext(TestContext testContext)
            throws IllegalAccessException, InvocationTargetException {

        if (applicationContext == null) {
            applicationContext = (ApplicationContext) ReflectionUtils.invokeMethod(GET_APPLICATION_CONTEXT,
                    testContext);
        }
        return applicationContext;
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        boolean exist = getApplicationContext(testContext).containsBean("globalModeHolder");
        if (exist) {
            this.globalModeHolder = getApplicationContext(testContext).getBean(GlobalModeHolder.class);
        }
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        Method testMethod = testContext.getTestMethod();
        Class<?> testClass = testContext.getTestClass();
        MockContext.getContext().setTestMethod(testMethod.getName());
        MockContext.getContext().setTestClass(testClass.getName());
        MockContext.getContext().setRpcOrder(1);
        MockContext.getContext().setDbOrder(1);
        //未配全局模式,方法级别
        if (globalModeHolder == null) {
            Mode mode = testMethod.getDeclaredAnnotation(Mode.class);
            if (Objects.nonNull(mode)) {
                MockContext.getContext().setMode(mode.value());
            } else {
                MockContext.getContext().setMode(TestMethodMode.NORMAL);
            }
        }
        //全局为重放模式，则测试的方法为重放模式
        if (globalModeHolder != null && globalModeHolder.getGlobalMode() == GlobalMode.REPLAY) {
            MockContext.getContext().setMode(TestMethodMode.REPLAY);
        }
        //全局为正常模式，则方法的模式覆盖全局模式
        if (globalModeHolder != null && globalModeHolder.getGlobalMode() == GlobalMode.NORMAL) {
            Mode mode = testMethod.getDeclaredAnnotation(Mode.class);
            if (Objects.nonNull(mode)) {
                MockContext.getContext().setMode(mode.value());
            } else {
                MockContext.getContext().setMode(TestMethodMode.NORMAL);
            }
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if ((MockContext.getContext().getMode() == TestMethodMode.REC)) {
            saveData();
        }
        MockContext.getContext().removeContext();
    }

    private void saveData() {
        //获取rpc的返回数据
        String fileName = MockFileHelper.getMockFile(MockContext.getContext().getCaseIndex());
        //检查文件是否存在，不存在，新生成
        File file = checkAndCreateFile(fileName);
        try {
            appendFile(file, JSON.toJSONString(MockContext.getContext().getMockData()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 普通写文件
     * @param file    文件名
     * @param context 内容
     * @throws IOException
     */
    private void appendFile(File file, String context) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.append(context);
        writer.close();
    }

    /**
     * 文件不存在时，创建文件
     * @param fileName
     */
    private File checkAndCreateFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return file;
        }
        try {
            Files.createParentDirs(file);
            Files.touch(file);
            return file;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
    }
}

