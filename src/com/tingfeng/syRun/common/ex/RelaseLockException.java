package com.tingfeng.syRun.common.ex;

/**
 * 用来主动释放锁
 */
public class RelaseLockException extends  MyException {

    public RelaseLockException(String msg) {
        super(msg);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
}
