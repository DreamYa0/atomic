package com.atomic.autotest.bizTools;

import cn.hutool.core.util.RandomUtil;

import java.math.BigDecimal;

public class randomBigType {
    public static BigDecimal getRandomBigDecimal(BigDecimal min, BigDecimal max) {
        BigDecimal bd = RandomUtil.randomBigDecimal(min, max);

        return bd.setScale(2,BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal getRandomBigDecimal(String minStr, String maxStr) {
        BigDecimal min = new BigDecimal(minStr);
        BigDecimal max = new BigDecimal(maxStr);
        BigDecimal bd = RandomUtil.randomBigDecimal(min, max);

        return bd.setScale(2,BigDecimal.ROUND_HALF_UP);
    }
}
