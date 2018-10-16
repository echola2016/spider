package com.cloudeye.doctor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudeye.util.EmailUtil;
import com.cloudeye.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by lafangyuan on 2018/10/9.
 */
@Slf4j
@Component
public class DoctorIndex implements CommandLineRunner{

    @Autowired
    private EmailUtil emailUtil;
    private boolean flag=true;

    String url ="http://appoint.yihu.com/appoint/do/doctorArrange/getArrangeWater";

    Map<String,String> data = new HashMap<>();
    public void spider(){
        data.put("doctorSn","710908823");
        data.put("hospitalId","304");
        data.put("channelId","1000025");
  /*      data.put("doctorSn","710908390");
        data.put("hospitalId","304");
        data.put("channelId","1000025");*/

        String result = HttpUtils.httpPost(url,data);
        checkNewData(result);
    }

    public void checkNewData(String result){
        JSONObject json = JSONObject.parseObject(result);
        if(json.getInteger("Code")==10000){
            JSONArray array = json.getJSONArray("Result");
            for(int i = 0;i<array.size();i++){
                JSONObject data = array.getJSONObject(i);
                String registerdate= data.getString("registerdate");
                if(data.getInteger("ArrangeStatus")==1){
                    if(data.getInteger("OverTime")==1){
                        log.info("{}:截止预约",registerdate);
                    }else if(data.getInteger("availablenum")<=0){
                        log.info("{}:约满、约满后补",registerdate);
                    }else {
                        if(data.getBooleanValue("UnOpened")){
                            log.info("{}:预约登记,预计{}放号",registerdate,data.getString("FHTimes"));
                        }else if(data.getIntValue("ModeId")==1){
                            StringBuffer text = new StringBuffer();
                            text.append(registerdate).append("--").append(data.getString("doctorname"))
                                    .append("可预约，剩余号：").append(data.getInteger("availablenum"));
                            log.info(text.toString());
                            emailUtil.send("327275033@qq.com",text.toString(),text.toString());
                            flag = false;
                        }
                    }
                }
            }
        }

    }

    public void watching(){
        long sleepTime = 1000*60*5+(long) (Math.random()*1000*60);
        while (flag){
           spider();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run(String... strings) throws Exception {
       watching();
//        emailUtil.send("327275033@qq.com","1","1");
    }
}
