package com.tingfeng.syRun.common.ex;

/**
 * 发送消息失败异常
 */
public class SendFailException extends  RuntimeException {

    public SendFailException(String msg) {
        super(msg);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
}
