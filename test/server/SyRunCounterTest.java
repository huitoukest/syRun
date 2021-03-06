package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.tingfeng.syrun.client.SyRunTCPClient;
import org.junit.Before;
import org.junit.Test;

import com.tingfeng.syrun.client.bean.FrequencyBean;
import com.tingfeng.syrun.client.util.SyRunClientUtil;
import com.tingfeng.syrun.common.FrequencyControlHelper;
import com.tingfeng.syrun.common.ex.OverRunTimeException;
import com.tingfeng.syrun.server.controller.CounterController;


public class SyRunCounterTest{
	static int counter = 0;
	CounterController signleRunController = null;
	
	@Before
	public void intit() {
		this.signleRunController = new CounterController();
	}
	
	@Test
	public void testSingleStep() throws IOException, InterruptedException, OverRunTimeException {
		int threadPoolSize = 200;
		//开启一个线程池，指定线程池的大小
        ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
        //指定方法完成的执行器
         ExecutorCompletionService<List<Map<String, Object>>> completion 
															= new ExecutorCompletionService<List<Map<String, Object>>>(service);
        final String key = System.currentTimeMillis()+"";
        signleRunController.initCounter(key, 0, System.currentTimeMillis() + 1000 * 60 * 10);
        final Map<String ,Integer> countMap = new HashMap<String ,Integer>();
        countMap.put("count", 0);
        long start = System.currentTimeMillis();
        try{
        	 for (int i=0;i<threadPoolSize;i++) {
	         //提交任务，提交后会默认启动Callable接口中的call方法
	         completion.submit(new Callable<List<Map<String,Object>>>() {			
				@Override
				public List<Map<String, Object>> call() throws Exception {
					int  k = SyRunClientUtil.doSingeStepWorkByCounter(
							new FrequencyControlHelper<Integer>(new FrequencyBean(1000*1200L,1)) {
								@Override
								public  Integer doWork() {
									//synchronized (countMap) {
									for(int i = 0;i < 100; i++) {
										if(i%5 == 0) {
											try {
												Thread.sleep(1);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										int cc  = countMap.get("count");
										countMap.put("count", cc + 1);
										System.out.println(cc + 1);
									}
									
									//}
									return  countMap.get("count");
								}

								@Override
								public long getExpireTime(String key) throws Exception {
									return signleRunController.getCounterExpireTime(key);
								}

								@Override
								public long addCounterValue(String key, int value) throws Exception {
									return signleRunController.addCounterValue(key, value);
								}

								@Override
								public void setExpireTime(String key, long expireTime) throws Exception {
									signleRunController.setCounterExpireTime(key, expireTime);
								}
						
					}, key,150);
					return null;
				}
			});
        	}
            //保证这些并发参数执行完毕后，再回到主线程。
            for (int i=0;i<threadPoolSize;i++) {
                try {
                    //返回内部具体委托的执行对象
                    Future<List<Map<String, Object>>> a = completion.take();
                    a.get();//等待任务执行结束，然后获得返回的结果
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                	e.printStackTrace();
                }
            }
        } finally {
            service.shutdown();
        }
        long end = System.currentTimeMillis();
        System.out.println("\n\n signleRunController count:"+ signleRunController.getCounterValue(key));
        System.out.println("\n\ncount:"+ countMap.get("count"));
        System.out.println("\nuseTime:"+(end - start));
	}
	
	
	@Test
	public void testAddStep() throws IOException, InterruptedException, OverRunTimeException {
		int threadPoolSize = 200;
		//开启一个线程池，指定线程池的大小
        ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
        //指定方法完成的执行器
         ExecutorCompletionService<String> completion = new ExecutorCompletionService<String>(
                service);
         signleRunController.initCounter("redis:hsh:test:count0", 0,System.currentTimeMillis() + 1000 * 60 * 10);
         
         final Map<String ,Integer> countMap = new HashMap<String ,Integer>();
        countMap.put("count", 0);
        long start = System.currentTimeMillis();
        try{
        	 for (int i=0;i<threadPoolSize;i++) {
	         //提交任务，提交后会默认启动Callable接口中的call方法
	         completion.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					for(int i = 0 ;i < 1000 ; i++) { 
						long re1  = signleRunController.addCounterValue("redis:hsh:test:count0", 2);
						//signleRunController.addCounterValue("redis:hsh:test:count0", -1);
						Thread.sleep(1);
						long re2  =  signleRunController.addCounterValue("redis:hsh:test:count0", -1);
						System.out.println("re1:" + re1 + " ,re2:" + re2);
					}
					 return "";
				}				
	        	
			});
        	}
            //保证这些并发参数执行完毕后，再回到主线程。
            for (int i=0;i<threadPoolSize;i++) {
                try {
                    //返回内部具体委托的执行对象
                    Future<String> a = completion.take();
                    a.get();//等待任务执行结束，然后获得返回的结果
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            service.shutdown();
           // SyRunTCPClient.closeConnect();
        }
        long end = System.currentTimeMillis();
        System.out.println("\n\ncount:"+ signleRunController.getCounterValue("redis:hsh:test:count0"));
        System.out.println("\nuseTime:"+(end - start));
	}
}
