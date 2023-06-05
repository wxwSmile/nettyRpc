package com.example.rpcnettyserver.rpc.base;

import com.example.rpcnettyserver.remote.SendSms;
import com.example.rpcnettyserver.rpc.base.server.ServerInit;
import com.example.rpcnettyserver.rpc.base.vo.NettyConstant;
import com.example.rpcnettyserver.rpc.sms.SendSmsImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * netty rpc框架 交给spring 管理
 * 包括netty组件的初始化 端口监听 实际服务注册
 */
@Slf4j
@Service
public class RpcServerFrame implements Runnable {
    @Resource
    private RegisterService registerService;
    @Resource
    private ServerInit serverInit;

    // 主线程组
    private EventLoopGroup boss = new NioEventLoopGroup();

    // 工作线程组
    private EventLoopGroup work = new NioEventLoopGroup();

    public void bind() throws Exception {
        // 链式调用 给bootstrap 赋值
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT))
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(serverInit);
        bootstrap.bind().sync();
        log.info("网络服务已准备好，可以进行业务操作了....... :" +
                NettyConstant.REMOTE_IP  +
        ":" + NettyConstant.REMOTE_PORT);
    }

    @PostConstruct
    public void startNet() throws Exception {
        // 注册服务
        registerService.regService(SendSms.class.getName(), SendSmsImpl.class);
        new Thread(this).start();
    }
    @PreDestroy
    public void stopNet() throws Exception {
        // 关闭服务
        boss.shutdownGracefully().sync();
        work.shutdownGracefully().sync();
    }


    @Override
    public void run() {
        try {
            bind();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
