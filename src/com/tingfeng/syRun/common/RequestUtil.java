package com.tingfeng.syRun.common;

import com.tingfeng.syRun.common.util.IdWorker;

public class RequestUtil {

    /**
     * 是否是异步信息
     * @return
     */
    public static boolean isAsychronizedMsg(String msg){
        if(null != msg && msg.startsWith(RequestType.ASY.getIdPrefix())){
            return true;
        }
        return false;
    }

    /**
     * 是否是同步消息
     * @param msg
     * @return
     */
    public static boolean isSychronizedMsg(String msg){
        if(null != msg && msg.startsWith(RequestType.SY.getIdPrefix())){
            return true;
        }
        return false;
    }

    /**
     * 获取一个同步消息的随机Id
     * @return
     */
    public static String getSychronizedMsgId(){
        return RequestType.SY.getIdPrefix() + IdWorker.getUUID();
    }

    /**
     * 获取一个异步消息的随机Id
     * @return
     */
    public static String getASychronizedMsgId(){
        return RequestType.ASY.getIdPrefix() + IdWorker.getUUID();
    }

}
