package com.tingfeng.syrun.server.bean;

import java.util.concurrent.CountDownLatch;

public class SyLockStatusBean {
    public String lockId = "";
    public CountDownLatch countDownLatch = null;
}
