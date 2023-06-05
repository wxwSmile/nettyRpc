package com.example.rpcnettyserver.rpc.base.server;

import com.example.rpcnettyserver.rpc.base.vo.MessageType;
import com.example.rpcnettyserver.rpc.base.vo.MyHeader;
import com.example.rpcnettyserver.rpc.base.vo.MyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

    // 检测用户是否重复登录的缓存
    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();
    // 用户白名单
    private String[] whiteList = {"127.0.0.1"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MyMessage message = (MyMessage) msg;
        // 如果是握手请求
        if (message.getMyHeader() != null &&  message.getMyHeader().getType() == MessageType.LOGIN_RESP.value())
        {
            String nodeIndex = ctx.channel().remoteAddress().toString();
            MyMessage loginRsp = null;
            // 重复登录 拒绝
            if (nodeCheck.containsKey(nodeIndex)) {
                loginRsp = buildResponse((byte)-1);
            } else {
                // 检查用户是否在单线程中
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String hostAddress = address.getAddress().getHostAddress();
                boolean isOK = false;
                for (String wip : whiteList) {
                    if (wip.equals(hostAddress)) {
                        isOK = true;
                        break;
                    }
                }
                loginRsp = isOK ? buildResponse((byte) 0) : buildResponse((byte) -1);
                if (isOK) {
                    nodeCheck.put(nodeIndex, true);
                }
            }
            log.info("the login response is :[{}], body :[{}]", loginRsp, loginRsp.getBody());
            ctx.writeAndFlush(loginRsp);
            ReferenceCountUtil.release(message);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private MyMessage buildResponse(byte b) {
        MyMessage message = new MyMessage();
        MyHeader myHeader = new MyHeader();
        myHeader.setType(MessageType.LOGIN_RESP.value());
        message.setMyHeader(myHeader);
        message.setBody(b);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常删除缓存
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
