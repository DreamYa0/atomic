package com.atomic.runner;

import org.testng.ITestNGMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2018/05/30 10:48
 */
public class PackagesAndGroupsHandler implements IHandler {

    private IHandler handler;

    @Override
    public Boolean handle(ITestNGMethod testNGMethod) {
        MethodSelectorConfig config = MethodSelectorConfig.getInstance();
        List<String> packageList = config.getPackageList();
        List<String> groupList = config.getGroupList();
        if (packageList.size() > 0 && groupList.size() > 0) {
            final boolean[] isInclude = {false};
            packageList.forEach(pack -> {
                if (testNGMethod.getRealClass().getName().contains(pack)) {
                    groupList.forEach(group -> {
                        Method method = testNGMethod.getConstructorOrMethod().getMethod();
                        Test test = method.getAnnotation(Test.class);
                        if (test != null) {
                            String[] groupNames = test.groups();
                            Arrays.stream(groupNames).forEach(name -> {
                                if (name.equals(group)) {
                                    isInclude[0] = true;
                                    return;
                                }
                            });
                        }
                    });
                }
            });
            if (!isInclude[0] && handler != null) {
                return handler.handle(testNGMethod);
            } else {
                return isInclude[0];
            }
        } else {
            if (handler != null) {
                return handler.handle(testNGMethod);
            }
        }
        return false;
    }

    @Override
    public void setNextHandler(IHandler handler) {
        this.handler = handler;
    }
}
