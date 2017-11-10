package com.tingfeng.syRun.client.util;

import com.tingfeng.syRun.common.bean.response.ResponseBean;

import java.io.IOException;

/**
 * 自定义的消息回调
 */
@FunctionalInterface
public interface MsgHandler {
    public void handMsg(ResponseBean msg);
}
