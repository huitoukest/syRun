package com.tingfeng.syRun.client.util;

import com.tingfeng.syRun.common.bean.response.ResponseBean;

/**
 * 自定义的消息回调
 */
@FunctionalInterface
public interface MsgHandler<T> {
    public T handMsg(ResponseBean msg);
}