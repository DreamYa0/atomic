package com.atomic.runtime;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

import java.util.List;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 测试方法运行规则选择器
 * @Data 2017/9/11 22:11
 */
public class RunTestSelector implements IMethodSelector {

    @Override
    public boolean includeMethod(IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod) {
        if (isTestMethod) {
            IHandler packAndGroupHandler = new PackagesAndGroupsHandler();
            IHandler packagesHandler = new PackagesHandler();
            IHandler groupsHandler = new GroupsHandler();
            IHandler methodsHandler = new ClassesHandler();
            packAndGroupHandler.setNextHandler(packagesHandler);
            packagesHandler.setNextHandler(groupsHandler);
            groupsHandler.setNextHandler(methodsHandler);
            return packAndGroupHandler.handle(method);
        }
        return true;
    }

    @Override
    public void setTestMethods(List<ITestNGMethod> testMethods) {

    }
}
