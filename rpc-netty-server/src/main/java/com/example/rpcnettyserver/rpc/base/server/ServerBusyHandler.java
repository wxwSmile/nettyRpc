package com.example.rpcnettyserver.rpc.base.server;

import com.example.rpcnettyserver.rpc.base.RegisterService;
import com.example.rpcnettyserver.rpc.base.vo.MessageType;
import com.example.rpcnettyserver.rpc.base.vo.MyHeader;
import com.example.rpcnettyserver.rpc.base.vo.MyMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@ChannelHandler.Sharable
public class ServerBusyHandler extends SimpleChannelInboundHandler<MyMessage> {

    @Resource
    private RegisterService registerService;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MyMessage myMessage) throws Exception {
        MyMessage message = new MyMessage();
        MyHeader handler = new MyHeader();
        handler.setSessionID(myMessage.getMyHeader().getSessionID());
        handler.setType(MessageType.SERVICE_RESP.value());
        message.setMyHeader(handler);

        Map<String,Object> content = (HashMap<String,Object>)myMessage.getBody();
        String serviceName = (String) content.get("siName");
        /*方法的名字*/
        String methodName = (String) content.get("methodName");
        /*方法的入参类型*/
        Class<?>[] paramTypes = (Class<?>[]) content.get("paraTypes");
        /*方法的入参的值*/
        Object[] args = (Object[]) content.get("args");

        /*从容器中拿到服务的Class对象*/
        Class serviceClass = registerService.getService(serviceName);
        if(serviceClass == null){
            throw new ClassNotFoundException(serviceName+ " not found");
        }

        Method method = serviceClass.getMethod(methodName, paramTypes);
        boolean result = (boolean) method.invoke(serviceClass.newInstance(), args);
        message.setBody(result);
        channelHandlerContext.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
       log.info(ctx.channel().remoteAddress() + "主动断开了连接");
    }
}
