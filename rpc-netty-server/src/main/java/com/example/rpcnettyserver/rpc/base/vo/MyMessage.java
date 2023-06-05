package com.example.rpcnettyserver.rpc.base.vo;

import lombok.Data;

@Data
public class MyMessage {
    private MyHeader myHeader;
    private Object body;
}
