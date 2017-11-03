package com.tingfeng.syRun.common;

/**
 * 业务消息的类型
 */
public enum MsgType {
    /**
     * 计数器
     */
    COUNTER(1),
    /**
     * 锁
     */
    LOCK(2),
    /**
     * 代码同步运行
     */
    SYRUN(3),
    /**
     * 计数器回滚
     */
    COUNTERROLLBACK(11),
    /**
     * 锁回滚
     */
    LOCKROLLBACK(12)
    ;
    private int value;

    private MsgType(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
