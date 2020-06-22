package com.atomic.param.values;

import com.atomic.tools.autotest.AutoTestEnum;
import com.google.common.collect.Lists;

import java.util.List;


public class AutoTestDoubleValues implements IAutoTestValues<Double> {

    @Override
    public List<Double> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(Double.MIN_VALUE, 0d, Double.MAX_VALUE);
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(Double.MIN_VALUE);
        }
        return Lists.newArrayList(null, Double.MIN_VALUE, -1d, 0d, Double.MAX_VALUE);
    }
}
