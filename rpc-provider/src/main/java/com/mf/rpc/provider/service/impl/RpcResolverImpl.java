package com.mf.rpc.provider.service.impl;

import com.mf.rpc.core.api.RpcResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RpcResolverImpl implements RpcResolver, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Override
    public Object resolve(String clazzName) {
        return this.applicationContext.getBean(clazzName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
