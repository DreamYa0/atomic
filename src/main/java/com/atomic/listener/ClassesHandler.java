package com.atomic.listener;

import com.atomic.config.MethodSelectorConfig;
import org.testng.ITestNGMethod;

import java.util.List;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2017/9/12 14:12
 */
public class ClassesHandler implements IHandler {

    private IHandler handler;

    @Override
    public Boolean handle(ITestNGMethod testNGMethod) {
        MethodSelectorConfig config = MethodSelectorConfig.getInstance();
        List<String> classList = config.getClassList();
        if (classList.size() > 0) {
            final boolean[] isInclude = {false};
            classList.forEach(className -> {
                Class clazz = testNGMethod.getRealClass();
                if (className.equals(clazz.getSimpleName())) {
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
