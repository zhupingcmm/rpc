package com.mf.rpc.provider.config;

import com.mf.rpc.api.UserService;
import com.mf.rpc.core.api.RpcResolver;
import com.mf.rpc.core.server.RpcInvoker;
import com.mf.rpc.provider.service.impl.RpcResolverImpl;
import com.mf.rpc.provider.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BeanConfig {
    @Bean
    public RpcInvoker createRpcInvoker(@Autowired RpcResolver resolver) {
        return new RpcInvoker(resolver);
    }

    @Bean
    public RpcResolver createRpcResolver() {
        return new RpcResolverImpl();
    }

    @Bean("com.mf.rpc.api.UserService")
    public UserService createUserService(){
        return new UserServiceImpl();
    }

}
