package com.example.rpcnettyclient.remote.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 *
 *类说明：用户的实体类，已实现序列化
 */
@Data
@AllArgsConstructor
public class UserInfo implements Serializable {
    private String name;
    private String phone;
}
