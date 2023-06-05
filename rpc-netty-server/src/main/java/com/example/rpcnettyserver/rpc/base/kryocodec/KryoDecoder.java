package com.example.rpcnettyserver.rpc.base.kryocodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 反序列化 扩展netty的message解码器
 */
public class KryoDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
       Object obj = KryoSerializer.deserialize(byteBuf);
       list.add(obj);
    }
}
