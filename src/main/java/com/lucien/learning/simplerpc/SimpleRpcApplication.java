package com.lucien.learning.simplerpc;

import com.lucien.learning.simplerpc.exporter.RpcExporter;
import com.lucien.learning.simplerpc.importer.RpcImporter;
import com.lucien.learning.simplerpc.service.LoginService;
import com.lucien.learning.simplerpc.service.impl.LoginServiceImpl;

import java.net.InetSocketAddress;

public class SimpleRpcApplication {

    public static void main(String[] args) {
        // 1. 启动服务提供端
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RpcExporter.exporter("localhost", 3000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

        // 2. 客户端构建动态代理累
        LoginService loginService = new RpcImporter<LoginService>().importer(LoginServiceImpl.class,
                new InetSocketAddress("localhost", 3000));
        // 3. 调用远程服务并输出结果
        System.out.println(loginService.loginIn("lucien","123123"));
    }

}
