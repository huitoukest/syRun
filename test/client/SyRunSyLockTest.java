package client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.bean.SyLockParam;
import com.tingfeng.syRun.bean.SyLockResponse;
import com.tingfeng.syRun.client.util.SyRunClientUtil;


public class SyRunSyLockTest{
	@Before
	public void initTest() {		
	}
	
	@Test
	public void testSyLock() {
		int threadPoolSize = 5;
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
					for(int idx = 0 ;idx < 2 ; idx++) {
						SyLockParam syLockParam = new SyLockParam();
						syLockParam.setKey(key);
						SyLockResponse response = JSONObject.parseObject(SyRunClientUtil.lockSyLock(key), SyLockResponse.class);					
						String lockId = response.getLockId();
						syLockParam.setLockId(lockId);
						Integer re1 = countMap.get("count");
						re1  = re1 + 2;
						if(idx % 5 ==0) {
							Thread.sleep(1);
						}
						Integer re2  = re1 - 1;
						countMap.put("count", re2);
						System.out.println("re1:" + re1 + " ,re2:" + re2);
						SyRunClientUtil.unlockSyLock(key, lockId);
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
