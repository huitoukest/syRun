package client;

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

import org.junit.Before;
import org.junit.Test;

import com.tingfeng.signleRun.client.SignleRunTCPClient;
import com.tingfeng.signleRun.client.bean.FrequencyBean;
import com.tingfeng.signleRun.client.util.SignleRunClientUtil;
import com.tingfeng.signleRun.common.FrequencyControlHelper;
import com.tingfeng.signleRun.common.ex.OutTimeException;
import com.tingfeng.signleRun.server.controller.CounterController;


public class SyRunSyLockTest{
	static int counter = 0;
	CounterController signleRunController = null;
	
	@Before
	public void intit() {
		this.signleRunController = new CounterController();
	}
	
	
	
	@Test
	public void testSingleStep() throws IOException, InterruptedException, OutTimeException{
		int threadPoolSize = 2000;
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
					int  k = SignleRunClientUtil.doSingeStepWorkByCounter(
							new FrequencyControlHelper(new FrequencyBean(1000*1200L,1)) {
								@Override
								public  Integer doWork() {
									//synchronized (countMap) {
									int cc  = countMap.get("count");
									countMap.put("count", cc+1);
									System.out.println(cc + 1);
									//}
									return  countMap.get("count");
								}

								@Override
								public long getExpireTime(String key) throws Exception {
									return Long.parseLong(signleRunController.getCounterExpireTime(key));
								}

								@Override
								public long addCounterValue(String key, int value) throws Exception {
									return Long.parseLong(signleRunController.addCounterValue(key, value));									
								}

								@Override
								public String setExpireTime(String key, long expireTime) throws Exception {
									return signleRunController.setCounterExpireTime(key, expireTime);		
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
        System.out.println("\n\ncount:"+countMap.get("count"));
        System.out.println("\nuseTime:"+(end - start));
	}
	
	
	@Test
	public void testAddStep() throws IOException, InterruptedException, OutTimeException{
		int threadPoolSize = 100;
		//开启一个线程池，指定线程池的大小
        ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
        //指定方法完成的执行器
         ExecutorCompletionService<String> completion = new ExecutorCompletionService<String>(
                service);
         SignleRunClientUtil.initCounter("redis:hsh:test:count0", 0,System.currentTimeMillis() + 1000 * 60 * 10);
         
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
						long re1  = SignleRunClientUtil.addCounterValue("redis:hsh:test:count0", 2);
						long re2  = SignleRunClientUtil.addCounterValue("redis:hsh:test:count0", -1);
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
            SignleRunTCPClient.closeConnect();
        }
        long end = System.currentTimeMillis();
        System.out.println("\n\ncount:"+ SignleRunClientUtil.getCounterValue("redis:hsh:test:count0"));
        System.out.println("\nuseTime:"+(end - start));
	}
}
