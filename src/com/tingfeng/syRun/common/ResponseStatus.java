package com.tingfeng.syrun.common;

/**
 *
 */
public enum ResponseStatus {
   FAIL(-1,"fail"),
   SUCCESS(0,"success"),
   OVERRUNTIME(1,"overRunTime"),
   OVERRESPONSETIME(2,"overResPonseTime"),
   CUSTOM(999,"custom msg");

   private String msg;
   private int value;

   private ResponseStatus(int value, String msg){
           this.msg = msg;
           this.value = value;
   }

    public String getMsg() {
        return msg;
    }

    public int getValue() {
        return value;
    }

}
