package com.atomic.runtime;

import com.atomic.config.MethodSelectorConfig;
import org.testng.ITestNGMethod;

import java.util.List;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2018/05/30 10:48
 */
public class PackagesHandler implements IHandler {

    private IHandler handler;

    @Override
    public Boolean handle(ITestNGMethod testNGMethod) {
        MethodSelectorConfig config = MethodSelectorConfig.getInstance();
        List<String> packageList = config.getPackageList();
        if (packageList.size() > 0) {
            final boolean[] isInclude = {false};
            packageList.forEach(pack -> {
                if (testNGMethod.getRealClass().getName().contains(pack)) {
                    isInclude[0] = true;
                    return;
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
