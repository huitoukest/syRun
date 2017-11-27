package server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.tingfeng.syrun.common.ResponseStatus;
import com.tingfeng.syrun.common.util.RequestUtil;
import com.tingfeng.syrun.common.bean.response.ResponseBean;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syrun.common.bean.request.SyLockParam;
import com.tingfeng.syrun.common.util.IdWorker;
import com.tingfeng.syrun.server.controller.SyLockController;

public class SyLockTest {
	private SyLockController syLockController = null;
	
	@Before
	public void initTest() {
		syLockController = new SyLockController();
	}
	
	@Test
	public void testSyLock() {
		int threadPoolSize = 1000;
		//开启一个线程池，指定线程池的大小
        ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
        //指定方法完成的执行器
         ExecutorCompletionService<String> completion = new ExecutorCompletionService<String>(
                service);
         final Map<String ,Integer> countMap = new HashMap<String ,Integer>();
        countMap.put("count", 0);
        long start = System.currentTimeMillis();
        String key = "test:" + start;
        try{
        	 for (int i=0;i<threadPoolSize;i++) {
	         //提交任务，提交后会默认启动Callable接口中的call方法
	         completion.submit(() -> {
					for(int idx = 0 ;idx < 50 ; idx++) {
						SyLockParam syLockParam = new SyLockParam();
						syLockParam.setKey(new String(key+""));
						String lockId  = syLockController.lockSyLock(RequestUtil.getSychronizedMsgId(), syLockParam);
						syLockParam.setLockId(lockId);
						Integer re1 = countMap.get("count");
						re1  = re1 + 2;
						if(idx % 20 ==0) {
							Thread.sleep(1);
						}
						Integer re2  = re1 - 1;
						countMap.put("count", re2);
						if(idx % 50 ==0) {
							System.out.println("re1:" + re1 + " ,re2:" + re2);
						}
						syLockController.unlockSyLock(RequestUtil.getSychronizedMsgId(), syLockParam);
					}
					 return "";
				});
        	}
            //保证这些并发参数执行完毕后，再回到主线程。
            for (int i=0;i<threadPoolSize;i++) {
            	Future<String> future = null;
                try {
                    //返回内部具体委托的执行对象
                    future = completion.take();
                    future.get();//等待任务执行结束，然后获得返回的结果
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }finally {
					if(null != future && !future.isCancelled()) {
						future.cancel(true);
					}
				}
            }
        } finally {
            service.shutdown();
        }
        long end = System.currentTimeMillis();
        System.out.println("\n\ncount:"+ countMap.get("count"));
        System.out.println("\nuseTime:"+(end - start));
	}
	
}
