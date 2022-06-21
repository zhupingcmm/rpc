package com.mf.rpc.provider.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zookeeper")
@Getter
@Setter
public class ZkConfig {
    private String host = "localhost";
    private int port = 2181;
    private String protocol = "http";
}
