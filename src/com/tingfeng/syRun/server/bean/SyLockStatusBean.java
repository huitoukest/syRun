package com.tingfeng.syRun.server.bean;

import java.util.concurrent.CountDownLatch;

public class SyLockStatusBean {
    public String lockId = "";
    public CountDownLatch countDownLatch = null;
}
