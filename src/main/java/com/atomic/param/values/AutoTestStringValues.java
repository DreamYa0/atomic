package com.atomic.param.values;

import com.atomic.enums.AutoTestEnum;
import com.google.common.collect.Lists;

import java.util.List;

public class AutoTestStringValues implements IAutoTestValues<String> {

    private static String longLength = null;

    @Override
    public List<String> getAutoTestValues(int autoTestValuesLevel) {
        if (longLength == null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                stringBuilder.append("a");
            }
            longLength = stringBuilder.toString();
        }
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList("a", longLength);
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(longLength);
        }
        return Lists.newArrayList(null, "", "a", longLength);
    }
}
