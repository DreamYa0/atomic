package com.atomic.param.values;

import com.atomic.enums.AutoTestEnum;
import com.google.common.collect.Lists;

import java.util.List;


public class AutoTestShortValues implements IAutoTestValues<Short> {

    @Override
    public List<Short> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(Short.MIN_VALUE, (short) 0, Short.MAX_VALUE);
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(Short.MIN_VALUE);
        }
        return Lists.newArrayList(null, Short.MIN_VALUE, (short) -1, (short) 0, Short.MAX_VALUE);
    }
}
