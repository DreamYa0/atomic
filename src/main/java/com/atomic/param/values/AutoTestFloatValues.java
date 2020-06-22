package com.atomic.param.values;

import com.atomic.tools.autotest.AutoTestEnum;
import com.google.common.collect.Lists;

import java.util.List;


public class AutoTestFloatValues implements IAutoTestValues<Float> {

    @Override
    public List<Float> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(Float.MIN_VALUE, 0f, Float.MAX_VALUE);
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(Float.MIN_VALUE);
        }
        return Lists.newArrayList(null, Float.MIN_VALUE, -1f, 0f, Float.MAX_VALUE);
    }
}
