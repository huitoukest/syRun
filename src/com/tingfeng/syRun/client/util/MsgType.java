package com.tingfeng.syRun.client.util;

/**
 * 业务消息的类型
 */
public enum MsgType {
    COUNTER(1),//计数器
    LOCK(2),//锁
    SYRUN(3)//代码同步运行
    ;
    private int value;

    private MsgType(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
