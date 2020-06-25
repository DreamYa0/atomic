package com.atomic.tools.rollback.db;

public interface DateValueContainer {

    /**
     * 返回日期
     * @return 返回日期
     */
    DateValue getDate();

    boolean isMidnight();

}
