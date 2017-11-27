import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syrun.common.bean.request.CounterParam;
import com.tingfeng.syrun.common.bean.request.RequestBean;
import org.junit.Test;

public class JsonTest {
    @Test
    public void testJsonSpeed(){
        RequestBean<CounterParam> requestBean = new RequestBean<>();
        requestBean.setId("123");
        requestBean.setParams(new CounterParam("addVaule",123123L,"keykeykey",System.currentTimeMillis()+10000));
        requestBean.setType(1);
        int jsonSize = 1000000;//1百万
        long start = System.currentTimeMillis();
        for(int i = 0 ;i < jsonSize ; i++ ){
            String tmp = JSONObject.toJSONString(requestBean);
            JSONObject json = JSONObject.parseObject(tmp);
            RequestBean<CounterParam> tmpRequestBean = new RequestBean<>();
            tmpRequestBean.setParams(json.getObject(json.getString("params"),CounterParam.class));
            if(0 == i % 10000){
                System.out.println("..." + i);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("use time " + (end - start) / 1000.0 + "秒");
    }
}
