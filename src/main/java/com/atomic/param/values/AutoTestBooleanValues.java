package com.atomic.param.values;

import com.atomic.tools.autotest.AutoTestEnum;
import com.google.common.collect.Lists;

import java.util.List;


public class AutoTestBooleanValues implements IAutoTestValues<Boolean> {

    @Override
    public List<Boolean> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(true, false);
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(false);
        }
        return Lists.newArrayList(null, true, false);
    }
}
