package com.tingfeng.syrun.client.util;

import com.tingfeng.syrun.common.bean.response.ResponseBean;

import java.io.IOException;

/**
 * 自定义的消息回调
 */
@FunctionalInterface
public interface MsgHandler {
    public void handMsg(ResponseBean responseBean);
}
