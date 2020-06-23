package com.atomic.runner;

import org.testng.ITestNGMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2017/9/12 14:11
 */
public class GroupsHandler implements IHandler {

    private IHandler handler;

    @Override
    public Boolean handle(ITestNGMethod testNGMethod) {
        MethodSelectorConfig config = MethodSelectorConfig.getInstance();
        List<String> groupList = config.getGroupList();
        if (groupList.size() > 0) {
            final boolean[] isInclude = {false};
            groupList.forEach(group -> {
                Method method = testNGMethod.getConstructorOrMethod().getMethod();
                Test test = method.getAnnotation(Test.class);
                if (test != null) {
                    String[] groupNames = test.groups();
                    for (String name : groupNames) {
                        if (name.equals(group)) {
                            isInclude[0] = true;
                            return;
                        }
                    }
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
