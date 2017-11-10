package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.tingfeng.syRun.client.SyRunTCPClient;
import com.tingfeng.syRun.client.util.MsgHandler;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import org.junit.Test;

import com.tingfeng.syRun.client.bean.FrequencyBean;
import com.tingfeng.syRun.client.util.SyRunClientUtil;
import com.tingfeng.syRun.common.FrequencyControlHelper;
import com.tingfeng.syRun.common.ex.OverRunTimeException;


public class SyRunCounterTest{
	
	static int counter = 0;
	@Test
	public void testSingleStep() throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException {
		int threadPoolSize = 200;
		//开启一个线程池，指定线程池的大小
        ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
        //指定方法完成的执行器
         ExecutorCompletionService<List<Map<String, Object>>> completion = new ExecutorCompletionService<List<Map<String, Object>>>(
                service);
         final String key = System.currentTimeMillis()+"";
         SyRunClientUtil.initCounter(key, 0,System.currentTimeMillis() + 1000 * 60 * 10);
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
									for(int i = 0 ;i < 20 ; i++) { 
										if(i % 5 ==0) {
											try {
												Thread.sleep(1);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										int cc  = countMap.get("count");
										countMap.put("count", cc+1);
										System.out.println(cc + 1);
									}
									Integer value = null;
									try {
										value = countMap.get("count");
									}catch (Exception e) {
										e.printStackTrace();
									}
									return  value;
								}

								@Override
								public long getExpireTime(String key) throws Exception {
									return SyRunClientUtil.getCounterExpireTime(key);
								}

								@Override
								public long addCounterValue(String key, int value) throws Exception {
									return SyRunClientUtil.addCounterValue(key, value);									
								}

								@Override
								public void setExpireTime(String key, long expireTime) throws Exception {
									SyRunClientUtil.setCounterExpireTime(key, expireTime);
								}
						
					}, key,300);
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
        System.out.println("\n\n SignleRunClientUtil count:"+ SyRunClientUtil.getCounterValue(key));
        System.out.println("\n\ncount:"+countMap.get("count"));
        System.out.println("\nuseTime:"+(end - start));
		SyRunTCPClient.closeConnect();
	}
	
	
	@Test
	public void testAddStep() throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException {
		final int threadPoolSize = 200;
		//开启一个线程池，指定线程池的大小
        ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
        //指定方法完成的执行器
         /*ExecutorCompletionService<String> completion = new ExecutorCompletionService<String>(
                service);*/
		SyRunClientUtil.initCounter("redis:hsh:test:count0", 0,System.currentTimeMillis() + 1000 * 60 * 10);
		final Map<String ,Integer> countMap = new HashMap<String ,Integer>();
        countMap.put("count", 0);
        long start = System.currentTimeMillis();
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        ArrayList<Future<String>> list = new ArrayList<>(15);
        	 for (int i=0;i<threadPoolSize;i++) {
                 //提交任务，提交后会默认启动Callable接口中的call方法
                 Future<String> future = service.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        for(int i = 0 ;i < 1000 ; i++) {
                            long re1  = SyRunClientUtil.addCounterValue("redis:hsh:test:count0", 2);
                            long re2  = SyRunClientUtil.addCounterValue("redis:hsh:test:count0", -1);
                            if(i % 1000 == 0){
                                System.out.println("re1:" + re1 + " ,re2:" + re2);
                            }
                        }
                         atomicInteger.incrementAndGet();
                        if(atomicInteger.addAndGet(0) == threadPoolSize){
                            System.out.println("complete all!");
                            service.shutdown();
                            long end = System.currentTimeMillis();
                            System.out.println("\n\ncount:"+ SyRunClientUtil.getCounterValue("redis:hsh:test:count0"));
                            System.out.println("\nuseTime:"+(end - start));
                            SyRunTCPClient.closeConnect();
                        }
                         return "";
                    }
                });
                 list.add(future);
        	}
        	list.stream().forEach(future-> {
                        try {
                            future.get(300,TimeUnit.SECONDS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            if(future.isCancelled()){
                                future.cancel(false);
                            }
                        }
                    }
            );
           /* //保证这些并发参数执行完毕后，再回到主线程。
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
            }*/
         /*finally {
            service.shutdown();
        }*/
	}

	/**
	 * 异步计数器测试
	 */
	@Test
	public void testAddStepByAsy() throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException {
		int threadPoolSize = 200;
        AtomicInteger atomicInteger = new AtomicInteger(0);
		//开启一个线程池，指定线程池的大小
		ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
		//指定方法完成的执行器
		ExecutorCompletionService<String> completion = new ExecutorCompletionService<String>(
				service);
		SyRunClientUtil.initCounter("redis:hsh:test:count0", 0,System.currentTimeMillis() + 1000 * 60 * 10,(ResponseBean msg)->{
            System.out.println(JSON.toJSONString(msg));
        });
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

							SyRunClientUtil.addCounterValue("redis:hsh:test:count0", 2, new MsgHandler() {
                                @Override
                                public void handMsg(ResponseBean response) {
                                    if(null == response || ResponseStatus.SUCCESS.getValue() != response.getStatus()){
                                        try {
                                            SyRunClientUtil.addCounterValue("redis:hsh:test:count0", 2,this);
                                        } catch (Exception e) {
                                            System.out.println("add counter error !" + e);
                                        }
                                    }else{
                                        countMap.put("count", Integer.parseInt(response.getData()));
                                    }
                                    atomicInteger.addAndGet(1);
                                }
                            });
							SyRunClientUtil.addCounterValue("redis:hsh:test:count0", -1,new MsgHandler() {
                                @Override
                                public void handMsg(ResponseBean response) {
                                    if(null == response || ResponseStatus.SUCCESS.getValue() != response.getStatus()){
                                        try {
                                            SyRunClientUtil.addCounterValue("redis:hsh:test:count0", -1,this);
                                        } catch (Exception e) {
                                            System.out.println("add counter error !" + e);
                                        }
                                    }else{
                                        countMap.put("count", Integer.parseInt(response.getData()));
                                    }
                                    atomicInteger.addAndGet(1);
                                }
                            });
							System.out.println("countMap:" + countMap.get("count"));
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
		}
		while (atomicInteger.addAndGet(0) < 200000){

		    Thread.sleep(100);
		    if(System.currentTimeMillis() % 1000 == 0){
		        System.out.println("sleep and wait ... ");
		    }
        }

        long end = System.currentTimeMillis();
		System.out.println("\n\ncount:"+ SyRunClientUtil.getCounterValue("redis:hsh:test:count0"));
		System.out.println("\nuseTime:"+(end - start));
		SyRunTCPClient.closeConnect();
	}
}
