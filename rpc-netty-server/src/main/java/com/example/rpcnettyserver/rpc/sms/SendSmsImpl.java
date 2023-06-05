package com.example.rpcnettyserver.rpc.sms;

import com.example.rpcnettyserver.remote.SendSms;
import com.example.rpcnettyserver.remote.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信消息发送服务
 */
@Service
@Slf4j
public class SendSmsImpl implements SendSms {
    @Override
    public boolean sendMail(UserInfo user) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("已发送短信给:[{}]到",user.getName(), user.getPhone());
        return true;
    }
}
