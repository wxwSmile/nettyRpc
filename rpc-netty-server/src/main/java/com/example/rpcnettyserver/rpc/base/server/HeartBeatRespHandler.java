package com.example.rpcnettyserver.rpc.base.server;

import com.example.rpcnettyserver.rpc.base.vo.MessageType;
import com.example.rpcnettyserver.rpc.base.vo.MyHeader;
import com.example.rpcnettyserver.rpc.base.vo.MyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MyMessage message = (MyMessage) msg;
        // 返回心跳信息
        if (message.getMyHeader() != null && message.getMyHeader().getType() == MessageType.HEARTBEAT_REQ.value()) {
            MyMessage heartBeat = buildHeatBeat();
            ctx.writeAndFlush(heartBeat);
            ReferenceCountUtil.release(msg);
        } else {
            ctx.fireChannelRead(message);
        }
    }

    private MyMessage buildHeatBeat() {
        MyMessage message = new MyMessage();
        MyHeader myHeader = new MyHeader();
        myHeader.setType(MessageType.HEARTBEAT_RESP.value());
        message.setMyHeader(myHeader);
        return message;
    }
}
