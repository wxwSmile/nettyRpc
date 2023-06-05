package com.example.rpcnettyclient.rpc.base.kryocodec;

import com.example.rpcnettyclient.rpc.base.vo.MyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class KryoEncoder extends MessageToByteEncoder<MyMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MyMessage message, ByteBuf byteBuf) throws Exception {
        KryoSerializer.serialize(message, byteBuf);
        channelHandlerContext.flush();
    }
}
