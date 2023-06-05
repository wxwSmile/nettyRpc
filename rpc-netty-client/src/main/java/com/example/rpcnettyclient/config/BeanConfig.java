package com.example.rpcnettyclient.config;

import com.example.rpcnettyclient.remote.SendSms;
import com.example.rpcnettyclient.rpc.RpcClientFrame;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class BeanConfig {

    @Resource
    private RpcClientFrame rpcClientFrame;

    @Bean
    public SendSms getSmsService() throws Exception {
        return rpcClientFrame.getRemoteProxyObject(SendSms.class);
    }
}
