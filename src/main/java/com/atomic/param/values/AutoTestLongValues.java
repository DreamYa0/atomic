package com.atomic.param.values;

import com.atomic.enums.AutoTestEnum;
import com.google.common.collect.Lists;

import java.util.List;


public class AutoTestLongValues implements IAutoTestValues<Long> {

    @Override
    public List<Long> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(Long.MIN_VALUE, 0L, Long.MAX_VALUE);
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(Long.MIN_VALUE);
        }
        return Lists.newArrayList(null, Long.MIN_VALUE, -1L, 0L, Long.MAX_VALUE);
    }
}
