package com.example.rpcnettyclient;

import com.example.rpcnettyclient.remote.SendSms;
import com.example.rpcnettyclient.remote.vo.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RpcNettyClientApplicationTestsInit {

    @Resource
    private SendSms sendSms;

    @Test
    void contextLoads() throws InterruptedException {
        long start = System.currentTimeMillis();
        /*发送邮件*/
        UserInfo userInfo = new UserInfo("smile","sky@geely.com");
        System.out.println("Send mail: "+ sendSms.sendMail(userInfo));
        System.out.println("共耗时："+(System.currentTimeMillis()-start)+"ms");
        Thread.sleep(3000);
    }

}
