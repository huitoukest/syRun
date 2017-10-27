package com.tingfeng.syRun.common.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CheckUtil {

    /**
     * 判断是否是空/空串
     * @param s
     * @return
     */
    public static boolean isNullString(String s){
        if(null == s || s.length() < 1){
            return true;
        }
        return false;
    }

    /**
     * 判断普通对象/字符串/数组/List/Map/Set是否是空
     * @param obj
     * @return
     */
    public static boolean isNull(Object obj){
        if(null == obj){
            return true;
        }
        if(obj instanceof String && ((String)obj).length() < 1){
            return true;
        }
        if(obj instanceof Object[] && ((Object[])obj).length < 1){
            return true;
        }
        if(obj instanceof List && ((List)obj).size() < 1){
            return true;
        }
        if(obj instanceof Map && ((Map)obj).size() < 1){
            return true;
        }

        if(obj instanceof Set && ((Set)obj).size() < 1){
            return true;
        }

        return false;
    }
}
