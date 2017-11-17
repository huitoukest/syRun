package com.tingfeng.syRun.common.ex;

/**
 * 用来主动释放锁
 */
public class ReleaseLockException extends  MyException {

    public ReleaseLockException(String msg) {
        super(msg);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
}
