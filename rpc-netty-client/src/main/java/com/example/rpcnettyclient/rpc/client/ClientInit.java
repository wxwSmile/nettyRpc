package com.example.rpcnettyclient.rpc.client;

import com.example.rpcnettyclient.rpc.base.kryocodec.KryoDecoder;
import com.example.rpcnettyclient.rpc.base.kryocodec.KryoEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ClientInit extends ChannelInitializer<SocketChannel> {

    @Resource
    private ClientBusyHandler clientBusyHandler;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //剥离接收到的消息的长度字段
        socketChannel.pipeline().addLast("frameDecoder",
                new LengthFieldBasedFrameDecoder(65535,
                        0,2,0,
                        2));
        socketChannel.pipeline().addLast("frameEncoder",
                new LengthFieldPrepender(2));
        //反序列化
        socketChannel.pipeline().addLast(new KryoDecoder());
        //序列化
        socketChannel.pipeline().addLast("MessageEncoder", new KryoEncoder());
        // 超时检测
        socketChannel.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
        // 发出登录请求
        socketChannel.pipeline().addLast("LoginAuthHandler", new LoginAuthReqHandler());
        // 发出心跳请求
        socketChannel.pipeline().addLast("HeartBeatHandler", new HeartBeatReqHandler());
        //业务处理
        socketChannel.pipeline().addLast("ClientBusiHandler", clientBusyHandler);
    }
}
