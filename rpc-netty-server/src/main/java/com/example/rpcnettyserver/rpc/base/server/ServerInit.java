package com.example.rpcnettyserver.rpc.base.server;

import com.example.rpcnettyserver.rpc.base.kryocodec.KryoDecoder;
import com.example.rpcnettyserver.rpc.base.kryocodec.KryoEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@Slf4j
public class ServerInit extends ChannelInitializer<SocketChannel> {
    @Resource
    private ServerBusyHandler serverBusyHandler;
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //构建handler netty 网络传输字节 1046  剥离接收到的消息的长度字段，拿到实际的消息报文的字节数组
        channel.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
                65535, 0, 2, 0, 2));

        // 发送的字段增加长度字段
        channel.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
        //反序列化
        channel.pipeline().addLast(new KryoDecoder());
        //序列化
        channel.pipeline().addLast("MessageEncoder", new KryoEncoder());
        // 超时检测
        channel.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
        //登录应答
        channel.pipeline().addLast(new  LoginAuthRespHandler());
        //心跳检查
        channel.pipeline().addLast("HeartBeatHandler", new HeartBeatRespHandler());
        // 服务端业务处理
        channel.pipeline().addLast("ServerBusyHandler", serverBusyHandler);
    }
}
