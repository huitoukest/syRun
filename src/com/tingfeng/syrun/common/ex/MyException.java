package com.tingfeng.syrun.common.ex;

/**
 * 自定义标记的异常
 */
public class MyException extends  RuntimeException {

    public MyException(String msg) {
        super(msg);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
}
