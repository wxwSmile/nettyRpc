package com.example.rpcnettyclient.rpc.client;

import com.example.rpcnettyclient.rpc.base.vo.MessageType;
import com.example.rpcnettyclient.rpc.base.vo.MyHeader;
import com.example.rpcnettyclient.rpc.base.vo.MyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MyMessage message = (MyMessage) msg;
        if (message.getMyHeader() != null && message.getMyHeader().getType() == MessageType.LOGIN_RESP.value()) {
           heartBeat =  ctx.executor().scheduleAtFixedRate(new HeartBeatReqHandler.HeartBeatTask(ctx), 0, 5000,
                    TimeUnit.MILLISECONDS);
            ReferenceCountUtil.release(msg);
        } else if (message.getMyHeader() != null &&  message.getMyHeader().getType() == MessageType.HEARTBEAT_RESP.value()) {
            log.info("client receive server heart message");
            ReferenceCountUtil.release(msg);
        } else {// 如果是其他报文传递给后面的handler
             ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
       ctx.fireExceptionCaught(cause);
    }

    private class HeartBeatTask implements Runnable {
        private final ChannelHandlerContext ctx;
        private final AtomicInteger heartBeatCount;

        private HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
            heartBeatCount = new AtomicInteger(0);
        }

        @Override
        public void run() {
            // 心跳检查机制
            MyMessage heatBeat = buildHeatBeat();
            ctx.writeAndFlush(heatBeat);
        }
        private MyMessage buildHeatBeat() {
            MyMessage message = new MyMessage();
            MyHeader myHeader = new MyHeader();
            myHeader.setType(MessageType.HEARTBEAT_REQ.value());
            message.setMyHeader(myHeader);
            return message;
        }
    }

}
