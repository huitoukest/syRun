package com.tingfeng.syrun.common.ex;

/**
 * 自定义消息异常,用来向上层传递消息之用,一般不用来处理真正的异常
 */
public class InfoException extends  MyException {

    public InfoException(String msg) {
        super(msg);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
}
