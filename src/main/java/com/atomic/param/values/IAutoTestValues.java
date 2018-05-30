package com.atomic.param.values;

import java.util.List;

public interface IAutoTestValues<T> {

    /**
     * 获取自动化测试的值
     * @param autoTestValuesLevel 若参数过多，组合将爆炸
     * @return
     */
    List<T> getAutoTestValues(int autoTestValuesLevel);

}
