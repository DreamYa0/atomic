package com.atomic.param.values;

import com.atomic.tools.autotest.AutoTestEnum;
import com.google.common.collect.Lists;

import java.util.List;


public class AutoTestByteValues implements IAutoTestValues<Byte> {

    @Override
    public List<Byte> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(Byte.MIN_VALUE, (byte) 0, Byte.MAX_VALUE);
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(Byte.MIN_VALUE, (byte) 0);
        }
        return Lists.newArrayList(null, Byte.MIN_VALUE, (byte) -1, (byte) 0, Byte.MAX_VALUE);
    }
}
