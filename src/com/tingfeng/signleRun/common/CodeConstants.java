package com.tingfeng.signleRun.common;

public class CodeConstants {
	
	public interface RquestKey {
		public static String TYPE = "type";
		public static String PARAMS = "params";
		public static String ID = "id";//每条消息的唯一识别码
	}
	
	public interface RquestType {
		/**
		 * 计数器
		 */
		public static int COUNTER = 1;
		/**
		 * 代码同步执行
		 */
		public static int SYRUN = 2;
		/**
		 * 同步原子锁
		 */
		public static int  SYNLOCK = 3;
		/**
		 * 异步原子锁
		 */
		public static int ASYNLOCK = 4;
	}
	
	public interface Counter {
		public static String METHOD = "method";
		public static String KEY = "key";
		public static String VALUE = "value";
		public static String EXPIRETIME = "expireTime";
	}
	
	public interface SynLock {
		public static String METHOD = "method";
		public static String KEY = "key";
		public static String MAXWAITTIME = "maxWaitTime";
	}
	
	public interface Result {
		public static String FAIL = "fail";
		public static String SUCCESS = "success";
		public static String OVERTIME = "overTime";
	}
	
}
