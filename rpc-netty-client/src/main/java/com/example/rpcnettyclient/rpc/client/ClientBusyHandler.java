package com.example.rpcnettyclient.rpc.client;

import com.example.rpcnettyclient.rpc.base.vo.MessageType;
import com.example.rpcnettyclient.rpc.base.vo.MyHeader;
import com.example.rpcnettyclient.rpc.base.vo.MyMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@ChannelHandler.Sharable
public class ClientBusyHandler extends SimpleChannelInboundHandler<MyMessage> {
    private ChannelHandlerContext ctx;

    //阻塞队列
    private final ConcurrentHashMap<Long, BlockingQueue<Object>> responseMap =
            new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MyMessage myMessage) throws Exception {
        if (myMessage.getMyHeader() != null && myMessage.getMyHeader().getType() == MessageType.SERVICE_REQ.value()) {
            long sessionID = myMessage.getMyHeader().getSessionID();
            boolean result =  (boolean)myMessage.getBody();
            BlockingQueue<Object> msgQueue = responseMap.get(sessionID);
            msgQueue.put(result);
        }
    }

    public Object send(Object message) throws InterruptedException {
        if (ctx.channel() == null || !ctx.channel().isActive()) {
            throw new IllegalStateException("和服务器还未未建立起有效连接！" +
                    "请稍后再试！！");
        }
        MyMessage myMessage = new MyMessage();
        MyHeader myHeader = new MyHeader();
        Random random = new Random();
        long sessionId = random.nextLong() + 1;
        myHeader.setType(MessageType.HEARTBEAT_REQ.value());
        myMessage.setMyHeader(myHeader);
        myMessage.setBody(message);
        BlockingQueue<Object> msgQueue = new ArrayBlockingQueue<>(1);
        responseMap.put(sessionId, msgQueue);
        ctx.writeAndFlush(myMessage);
        Object result =  msgQueue.take();
        log.info("收到服务器端处理端结果 :[{}]",result);
        return result;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.ctx = ctx;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
