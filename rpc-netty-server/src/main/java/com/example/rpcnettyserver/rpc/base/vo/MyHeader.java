package com.example.rpcnettyserver.rpc.base.vo;

import lombok.Data;

@Data
public final class MyHeader {
    private int crcCode = 0xabef0101;
    private int length;// 消息长度
    private long sessionID;// 会话ID
    private byte type;// 消息类型
    private byte priority;// 消息优先级
}
