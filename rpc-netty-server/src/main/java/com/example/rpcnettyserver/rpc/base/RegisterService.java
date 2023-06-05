package com.example.rpcnettyserver.rpc.base;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegisterService {
    private static final Map<String,Class> serviceCache = new ConcurrentHashMap<>();
    public void regService(String serviceName, Class impl) {
        serviceCache.put(serviceName,impl);
    }
    public Class getService(String serviceName) {
        return serviceCache.get(serviceName);
    }
}
