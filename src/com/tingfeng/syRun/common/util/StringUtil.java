package com.tingfeng.syRun.common.util;

import com.tingfeng.syRun.common.bean.response.ResponseBean;
import org.slf4j.Logger;

import javax.xml.ws.Response;

public class StringUtil {


    public static long getLong(String data,Logger logger) throws NumberFormatException{
        long result = 0;
        try{
            result = Long.parseLong(data);
        }catch (Exception e){
            logger.error("解析结果出错.",e);
            throw e;
        }
        return result;
    }




}
