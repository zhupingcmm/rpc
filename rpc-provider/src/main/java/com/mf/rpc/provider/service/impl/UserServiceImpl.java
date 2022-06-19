package com.mf.rpc.provider.service.impl;

import com.mf.rpc.api.User;
import com.mf.rpc.api.UserService;


public class UserServiceImpl implements UserService {
    @Override
    public User findById(int id) {
        return new User(id, "kk" + System.currentTimeMillis());
    }
}
