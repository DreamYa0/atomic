package com.atomic.enums;

/**
 * 枚举类型被编码化
 */
public interface CodeableEnum {
    /**
     * 代码值
     * @return
     */
    String getValue();

    /**
     * 描述值
     * @return
     */
    String getDesc();
}
