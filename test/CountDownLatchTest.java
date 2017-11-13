import client.SyRunSyLockTest;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.util.IdWorker;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CountDownLatchTest {
    public static final  int threadSize = 500;
    private static final ExecutorService serviceMsgPool = Executors.newFixedThreadPool(threadSize);
    private static final ExecutorService cuMsgPool = Executors.newFixedThreadPool(100);
    /**
     * 初始的消息和线程池计数器Map大小
     */
    public static final int DATA_INIT_SIZE = 50000;

    /**
     * 消息池
     */
    private static final ConcurrentHashMap<String,ResponseBean> msgResPonseMap = new ConcurrentHashMap<>(DATA_INIT_SIZE);
    private static final ConcurrentHashMap<String,CountDownLatch> countDownLatchMap = new ConcurrentHashMap<>(DATA_INIT_SIZE);


    public static void receiveMsg(String id){
        ResponseBean responseBean = new ResponseBean();
        responseBean.id = id;
        receiveMsg(responseBean);
    }

    /**
     * 收到一条消息后,将此消息通过此处处理
     * @param responseBean
     */
    public static void receiveMsg(ResponseBean responseBean){
        Future<String> future= serviceMsgPool.submit(()->{
            try {
                Thread.sleep(1 + (long)( Math.random() * 2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String id = responseBean.getId();
            msgResPonseMap.put(id, responseBean);
                CountDownLatch countDownLatch = countDownLatchMap.get(id);
                if (null != countDownLatch) {
                    countDownLatchMap.remove(id);
                    countDownLatch.countDown();
                }
            return id;
        });
        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(future.isCancelled()){
                future.cancel(false);
            }

        }
    }



    /**
     * 同步的消息发送,效率低下
     * @param requestBean
     * @return
     */
    public static ResponseBean sendMsg(RequestBean<?> requestBean) throws InterruptedException {
        final String id = requestBean.getId();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatchMap.put(id,countDownLatch);
        receiveMsg(id);

        countDownLatch.await(10,TimeUnit.SECONDS);
        ResponseBean responseBean = msgResPonseMap.get(id);
        msgResPonseMap.remove(id);

        return responseBean;
    }


    public static ResponseBean sendMsgByCu(RequestBean<?> requestBean) throws InterruptedException {
        final String id = requestBean.getId();
        receiveMsg(id);
        Future<ResponseBean> future = cuMsgPool.submit(()->{
            ResponseBean responseBean =  null;
            do {
                responseBean = msgResPonseMap.get(id);
                if(null == requestBean){
                    Thread.sleep(1);
                }
            }while(null == responseBean);
            return responseBean;
        });

        ResponseBean responseBean = null;
        try {
            responseBean = future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(!future.isCancelled()){
                future.cancel(false);
            }
        }
        msgResPonseMap.remove(id);
        return responseBean;
    }




    @Test
    public void countDownLatchTest(){
           int threadSize = 500;
           final AtomicInteger atomicInteger = new AtomicInteger(0);
           long time = System.currentTimeMillis();
           for(int i = 0 ; i < threadSize ;i++){
               new Thread(()->{
                   for(int j = 0; j < 1000 ; j++){
                       RequestBean requestBean = new RequestBean();
                       requestBean.setId(IdWorker.getUUID() + "");
                       try {
                           //sendMsg(requestBean);
                           sendMsgByCu(requestBean);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
                   System.out.println(atomicInteger.incrementAndGet() + ", use time " + (System.currentTimeMillis() - time));
                   if(atomicInteger.addAndGet(0) == threadSize){
                       System.out.println("complete all!");
                       serviceMsgPool.shutdown();
                       cuMsgPool.shutdown();
                   }
               }).start();
           }
    }

    public static  void main(String[] args){
        CountDownLatchTest test =   new CountDownLatchTest();
        test.countDownLatchTest();//默认的效率测试


        //new SyRunSyLockTest().testSyLockByAsy();
    }



}

