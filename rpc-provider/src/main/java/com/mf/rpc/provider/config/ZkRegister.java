package com.mf.rpc.provider.config;

import lombok.RequiredArgsConstructor;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class ZkRegister {
    private final static String SERVER_PATH = "/servers";

    @Value("${spring.application.name}")
    private String applicationName;


    private final ZkConfig zkConfig;

    @PostConstruct
    public void init() {

        String zkUrl = zkConfig.getHost() + ":" + zkConfig.getPort();
//        String applicationUrl = applicationHost + ":" + applicationPost;
        String zkRootSever = "/" + applicationName;
        //启动时把server的信息注册到注册中心
        ZkClient zkClient = new ZkClient(zkUrl);
        zkClient.setZkSerializer(new ZkSerializer());
        Random random = new Random();
        int num = random.nextInt(10);
        if (!zkClient.exists(zkRootSever)) {
            zkClient.createPersistent(zkRootSever);
        }
        zkClient.createEphemeral(zkRootSever + "/" + num, "http://localhost:8091");
    }

}
