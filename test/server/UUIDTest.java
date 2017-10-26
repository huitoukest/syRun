package server;

import com.tingfeng.syRun.common.util.RequestUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UUIDTest {

    @Test
    public void uuidTest(){
        int poolSize = 10;
        ExecutorService service = Executors.newFixedThreadPool(poolSize);
        int testSize = 50000;
        Set<String> set = new HashSet<>(testSize);
        service.submit(() ->{
            for(int i =0 ; i < testSize;i++){
                    String id = RequestUtil.getSychronizedMsgId();
                    if(set.contains(id)){
                        System.out.println("error used id!");
                    }else{
                        set.add(id);
                    }
            }
        });
    }
}
