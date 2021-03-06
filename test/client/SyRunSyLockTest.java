package client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syrun.client.SyRunTCPClient;
import com.tingfeng.syrun.client.util.MsgHandler;
import com.tingfeng.syrun.common.ResponseStatus;
import com.tingfeng.syrun.common.bean.response.ResponseBean;
import org.junit.Before;
import org.junit.Test;

import com.tingfeng.syrun.common.bean.request.SyLockParam;
import com.tingfeng.syrun.client.util.SyRunClientUtil;


public class SyRunSyLockTest{
	@Before
	public void initTest() {		
	}
	
	@Test
	public void testSyLock() throws IOException {
		int threadPoolSize = 200;
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
						syLockParam.setKey(key);

						String lockId = null;
                        while (true){
                            try{
                                lockId = SyRunClientUtil.getLock(key);
                                break;
                            }catch (Exception e){
                                //e.printStackTrace();
                                Thread.sleep((int)(Math.random()*200));
                            }
                        }
						syLockParam.setLockId(lockId);
						Integer re1 = countMap.get("count");
						re1  = re1 + 2;
						if(idx % 20 ==0) {
							Thread.sleep(1);
						}
						Integer re2  = re1 - 1;
						countMap.put("count", re2);
						System.out.println("re1:" + re1 + " ,re2:" + re2);
						SyRunClientUtil.releaseLock(key, lockId);
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
		//SyRunTCPClient.closeConnect();
	}



	public static void main(String[] margs){
        new SyRunSyLockTest().testSyLockByAsy();
    }

	@Test
	public void testSyLockByAsy() {
		int threadPoolSize = 100;
		int threadPerSize = 50;
				//开启一个线程池，指定线程池的大小
		ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
		final Map<String ,Integer> countMap = new HashMap<String ,Integer>();
		countMap.put("count", 0);
		long start = System.currentTimeMillis();
		String key = "test:" + start;
		final AtomicInteger atomicInteger = new AtomicInteger(0);
			for (int i=0;i<threadPoolSize;i++) {
				//提交任务，提交后会默认启动Callable接口中的call方法
				service.submit(() -> {
					for(int idx = 0 ;idx < threadPerSize ; idx++) {
                        SyLockParam syLockParam = new SyLockParam();
                        syLockParam.setKey(key);
                        SyRunClientUtil.getLock(key, new MsgHandler() {
                            @Override
                            public void handMsg(ResponseBean responseBean) {

                                if (ResponseStatus.SUCCESS.getValue() != responseBean.getStatus()) {
                                    //System.out.println("lock error:" + responseBean.getErrorMsg());
                                    try {
                                        Thread.sleep((int)(Math.random()*300));
                                        SyRunClientUtil.getLock(key, this);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    syLockParam.setLockId(responseBean.getData());
                                    //System.out.println("lock result:" + JSONObject.toJSONString(responseBean));
                                    Integer re1 = countMap.get("count");
                                    re1 = re1 + 2;
                                    Integer re2 = re1 - 1;
                                    countMap.put("count", re2);
                                    System.out.println("re1:" + re1 + " ,re2:" + re2);
                                    try {
                                        SyRunClientUtil.releaseLock(key, responseBean.getData());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    atomicInteger.incrementAndGet();
                                }
                            }
                        });
                    }
                    return "";  //end for
				});
			}
		while(atomicInteger.addAndGet(0) < threadPoolSize * threadPerSize){
			try {
				Thread.sleep(200);
				if(System.currentTimeMillis() % 1000 == 0){
					System.out.println("waiting ... ," + atomicInteger.addAndGet(0) );
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		service.shutdown();

			SyRunTCPClient.closeConnect();

		long end = System.currentTimeMillis();
        System.out.println("\n\ncount:"+ countMap.get("count"));
		System.out.println("\nuseTime:"+(end - start));

	}
}
