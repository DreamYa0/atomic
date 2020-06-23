package com.atomic.param;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public final class CallBack {

    private CallBack() {

    }

    public static ITestResultCallback paramAndResultCallBack() {
        // 回调函数，为testCase方法传入，入参和返回结果
        return (param, result, parameters) -> {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    param.put(Constants.PARAMETER_NAME_ + i, parameters[i]);
                }
            }
            param.put(Constants.RESULT_NAME, result);
        };
    }
}
