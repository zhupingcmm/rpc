package com.mf.rpc.consumer;

import com.mf.rpc.api.User;
import com.mf.rpc.api.UserService;
import com.mf.rpc.core.client.RpcClient;

public class Consumer {
    public static void main(String[] args) {
        UserService userService = RpcClient.create(UserService.class, "http://localhost:8091");
        User user = userService.findById(1);
        System.out.println("user::" + user.getName());
    }
}
