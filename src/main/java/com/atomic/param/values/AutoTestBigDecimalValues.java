package com.atomic.param.values;

import com.atomic.enums.AutoTestEnum;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;


public class AutoTestBigDecimalValues implements IAutoTestValues<BigDecimal> {

    @Override
    public List<BigDecimal> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(new BigDecimal(-Long.MAX_VALUE), new BigDecimal(0),
                    new BigDecimal(Long.MAX_VALUE));
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(new BigDecimal(-Long.MAX_VALUE));
        }
        return Lists.newArrayList(null, new BigDecimal(-1), new BigDecimal(0),
                new BigDecimal(-Long.MAX_VALUE), new BigDecimal(Long.MAX_VALUE));
    }
}
