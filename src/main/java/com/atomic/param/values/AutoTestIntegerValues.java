package com.atomic.param.values;

import com.atomic.tools.autotest.AutoTestEnum;
import com.google.common.collect.Lists;

import java.util.List;


public class AutoTestIntegerValues implements IAutoTestValues<Integer> {

    @Override
    public List<Integer> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(Integer.MIN_VALUE, 0);
        }
        return Lists.newArrayList(null, Integer.MIN_VALUE, -10000, -1, 0, Integer.MAX_VALUE);
    }
}
