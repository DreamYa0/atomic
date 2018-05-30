package com.atomic.enums;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2017/8/13 16:57
 */
public enum AlgorithmEnum {

    RSA("RSA"),
    DES("DES"),
    AES("AES"),
    MD5("MD5"),
    SHA("SHA"),
    DSA("DSA");

    private String value;

    AlgorithmEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
