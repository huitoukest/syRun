package com.tingfeng.syRun.common;

public class CodeConstants {
	
	public interface RquestKey {
		public final static String TYPE = "type";
		public final static String PARAMS = "params";
		public final static String ID = "id";//每条消息的唯一识别码
	}
	
	public interface Counter {
		public final static String METHOD = "method";
		public final static String KEY = "key";
		public final static String VALUE = "value";
		public final static String EXPIRETIME = "expireTime";
	}
	
	public interface SynLock {
		public final static String METHOD = "method";
		public final static String KEY = "key";
		public final static String MAXWAITTIME = "maxWaitTime";
	}
	
}
