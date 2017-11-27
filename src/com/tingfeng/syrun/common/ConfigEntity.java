package com.tingfeng.syrun.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigEntity {

	private static Logger logger = LoggerFactory.getLogger(ConfigEntity.class);

	private static final ConfigEntity configEntity = new ConfigEntity();
    private static boolean isInit = false;
	private ConfigEntity(){

    }

	private  int serverTcpPort = 9999;
    private  String serverIp = "127.0.0.1";

    private  int timeOutConnect = 20000;//连接超时时间,默认20秒.
    private  int timeReconnect = 2000;//重连2秒内.

    private  int timeClientIdle = 30000;//客户端空闲时间30秒一个.
    private  int timeServerIdle = 30000;//服务器空闲时间30秒一个.
    private  int timeHeartBeat = 20000;//心跳包.

    private  int timeOutRun = 300000;//300秒的运行超时时间/锁占用阻塞.


    private  int timeResendIdle = 5000;//消息发送失败后的重发间隔时间.

    private  int serverHandlPoolSize = 256;//服务器处理消息收发的业务线程池大小
    private  int serverMaxLockSize = 200;//maxLockSize是最大lock阻塞的线程数量,所以必须小于serverHandlPoolSize,如果大于等于serverHandlPoolSize,将会导致服务器宕机问题.



    public synchronized static ConfigEntity getInstance(){
        if(!isInit) {
            loadProperties();
            isInit = true;
        }
        return configEntity;
    }

	/**
	 * 载入配置文件,默认只载入一次
	 */
	private synchronized static void loadProperties() {
		Properties pro = new Properties();
		InputStream in = null;
        try {
			in = ConfigEntity.class.getClassLoader().getResourceAsStream("syRun.properties");
			if(null == in){
                throw new FileNotFoundException();
            }
			pro.load(in);
            configEntity.serverTcpPort = Integer.parseInt(pro.getProperty("serverTcpPort","9999"));
            configEntity.serverIp = pro.getProperty("serverIp","127.0.0.1");
            configEntity.timeOutConnect = Integer.parseInt(pro.getProperty("timeOutConnect","20000"));
            configEntity.timeReconnect = Integer.parseInt(pro.getProperty("timeReconnect","2000"));
            configEntity.timeClientIdle = Integer.parseInt(pro.getProperty("timeClientIdle","30000"));
            configEntity.timeServerIdle = Integer.parseInt(pro.getProperty("timeServerIdle","30000"));
            configEntity.timeHeartBeat = Integer.parseInt(pro.getProperty("timeHeartBeat","20000"));
            configEntity.timeOutRun = Integer.parseInt(pro.getProperty("timeOutRun","300000"));
            //keySession = pro.getProperty("keySession","key_session");
            configEntity.timeResendIdle = Integer.parseInt(pro.getProperty("timeResendIdle","5000"));
            configEntity.serverHandlPoolSize = Integer.parseInt(pro.getProperty("serverHandlPoolSize","256"));
            configEntity.serverMaxLockSize = Integer.parseInt(pro.getProperty("serverMaxLockSize","200"));
            if(configEntity.serverHandlPoolSize <= configEntity.serverMaxLockSize ){
                configEntity.serverHandlPoolSize = configEntity.serverMaxLockSize + 1;
            }

		} catch (FileNotFoundException e) {
			logger.warn("not find syRun.properties , will run with default properties",e);
		} catch (IOException e) {
			logger.error("read syRun.properties error !",e);
			throw new RuntimeException(e);
		} catch (Exception e) {
            logger.error("read syRun.properties error !",e);
            throw new RuntimeException(e);
        } finally {
			try {
			    if(null != in) {
                    in.close();
                }
			} catch (IOException e) {
                logger.error("close read syRun.properties error !",e);
			}
		}
        logger.info("read syRun.properties success !");
	}

    public int getServerTcpPort() {
        return serverTcpPort;
    }

    public  String getServerIp() {
        return serverIp;
    }

    public  int getTimeOutConnect() {
        return timeOutConnect;
    }

    public  int getTimeReconnect() {
        return timeReconnect;
    }

    public  int getTimeHeartBeat() {
        return timeHeartBeat;
    }
    public int getTimeClientIdle() {
        return timeClientIdle;
    }

    public int getTimeServerIdle() {
        return timeServerIdle;
    }
    public  int getTimeOutRun() {
        return timeOutRun;
    }

    public  int getTimeResendIdle() {
        return timeResendIdle;
    }

    public int getServerHandlPoolSize() {
        return serverHandlPoolSize;
    }

    public int getServerMaxLockSize() {
        return serverMaxLockSize;
    }
}
