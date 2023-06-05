package com.example.rpcnettyclient.rpc.client;

import com.example.rpcnettyclient.rpc.base.vo.MessageType;
import com.example.rpcnettyclient.rpc.base.vo.MyHeader;
import com.example.rpcnettyclient.rpc.base.vo.MyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       ctx.writeAndFlush(buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MyMessage message = (MyMessage)msg;
        // 如果是握手应答消息 需要判断是否成功
        if (message.getMyHeader() != null && message.getMyHeader().getType() == MessageType.LOGIN_RESP.value()) {
           byte logonResult = (byte) message.getBody();
           if (logonResult != (byte) 0) {
               // 关闭连接
               ctx.close();
           } else {
               log.info("Login is ok :[{}]", msg);
               ctx.fireChannelRead(msg);
           }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

    private MyMessage buildLoginReq() {
        MyMessage message = new MyMessage();
        MyHeader header = new MyHeader();
        header.setType(MessageType.LOGIN_REQ.value());
        message.setMyHeader(header);
        return message;
    }
}
