package com.tingfeng.syrun.common;

/**
 * 发送和接收的请求类型
 */
public enum RequestType {
    /**
     * 同步请求
     */
    SY("sy_"),
    /**
     * 异步请求
     */
    ASY("asy_");

    private String idPrefix;

    private RequestType(String idPrefix){
        this.idPrefix = idPrefix;
    }

    public String getIdPrefix() {
        return idPrefix;
    }
}
