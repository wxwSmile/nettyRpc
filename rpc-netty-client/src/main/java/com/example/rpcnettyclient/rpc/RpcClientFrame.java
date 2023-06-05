package com.example.rpcnettyclient.rpc;

import com.example.rpcnettyclient.rpc.base.vo.NettyConstant;
import com.example.rpcnettyclient.rpc.client.ClientBusyHandler;
import com.example.rpcnettyclient.rpc.client.ClientInit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * rpc框架客户端代理部分 交给spring管理
 * 1 动态代理的实现中 不在连接服务器 而是直接发送
 * 2 客户端网络部分的主体 包括netty组件的初始化 连接服务器
 *
 */
@Slf4j
@Service
public class RpcClientFrame implements Runnable {
    private ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(1);

    private Channel channel;

    private EventLoopGroup group = new NioEventLoopGroup();

    //是否用户主动关闭连接的标志值
    private volatile boolean userCode =false;
    //连接是否成功关闭的标志值
    private volatile boolean connected = false;

    @Resource
    private ClientInit clientInit;
    @Resource
    private ClientBusyHandler clientBusyHandler;

    //远程服务的代理对象 参数为客户端要调用的服务
     public <T>  T getRemoteProxyObject(final Class<?> serviceInterface) {
        return (T)Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                 new Class<?>[]{serviceInterface},
                 new DynProxy(serviceInterface, clientBusyHandler));
     }

     private static class DynProxy implements InvocationHandler {
         private Class<?> serviceInterface;
         private ClientBusyHandler clientBusyHandler;
         public DynProxy(Class<?> serviceInterface, ClientBusyHandler clientBusyHandler) {
             this.serviceInterface = serviceInterface;
             this.clientBusyHandler = clientBusyHandler;
         }

         @Override
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
             Map<String,Object> content = new HashMap<>();
             content.put("siName", serviceInterface.getName());
             content.put("methodName", method.getName());
             content.put("paraTypes", method.getParameterTypes());
             content.put("args", args);
             return clientBusyHandler.send(content);
         }
     }

    @Override
    public void run() {
        try {
            connect(NettyConstant.REMOTE_PORT, NettyConstant.REMOTE_IP);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("client error :[{}]", e);
//            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void startNet() throws InterruptedException {
         new Thread(this).start();
         while (!this.isConnected()) {
             synchronized (this) {
                 this.wait();
             }
         }
        log.info("网络通信已准备好，可以进行业务操作了........");
    }

    @PreDestroy
    public void stopNet() {
         close();
    }

    public boolean isConnected () {
         return connected;
    }

    public void close() {
        userCode = true;
        channel.close();
    }

    // 连接服务器
    public void connect(int port, String host) throws Exception {
         try {
             Bootstrap bootstrap = new Bootstrap();
             bootstrap.group(group).channel(NioSocketChannel.class)
                     .option(ChannelOption.TCP_NODELAY, true)
                     .handler(clientInit);
             //发起异步连接
             ChannelFuture future =  bootstrap.connect(new InetSocketAddress("127.0.0.1", port)).sync();
             channel = future.sync().channel();
             // 连接成功后通知等待线程 连接已经简历
             synchronized (this) {
                 this.connected = true;
                 this.notifyAll();
             }
             future.channel().closeFuture().sync();
         } finally {
             // 非用户主动关闭连接 可能发生了网络问题 需要重新连接
             if (!userCode) {
                 log.info("发现异常，可能发生了服务器异常或网络问题，准备进行重连.....");
                 // 再次重联
                 executor.execute(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             TimeUnit.SECONDS.sleep(1);
                             try {
                                 connect(NettyConstant.REMOTE_PORT,
                                         NettyConstant.REMOTE_IP);
                             } catch (Exception e) {
                                 throw new RuntimeException(e);
                             }
                         } catch (InterruptedException e) {
                             throw new RuntimeException(e);
                         }

                     }
                 });
             } else {
                 // 用户主动关闭
                 channel = null;
                 group.shutdownGracefully().sync();
                 connected = false;
             }
         }
    }

}
