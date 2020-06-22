package com.atomic.param.values;

import com.atomic.tools.autotest.AutoTestEnum;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class AutoTestDateValues implements IAutoTestValues<String> {

    private String getDateString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    @Override
    public List<String> getAutoTestValues(int autoTestValuesLevel) {
        if (autoTestValuesLevel == AutoTestEnum.SMALL.getLevel()) {
            return Lists.newArrayList(getDateString(new Date(0)), getDateString(new Date()),
                    getDateString(new Date(Integer.MAX_VALUE)));
        } else if (autoTestValuesLevel == AutoTestEnum.VERY_SMALL.getLevel()) {
            return Lists.newArrayList(getDateString(new Date(Integer.MAX_VALUE)));
        }
        return Lists.newArrayList(null, getDateString(new Date(0)), getDateString(new Date()),
                getDateString(new Date(Integer.MAX_VALUE)));
    }
}
