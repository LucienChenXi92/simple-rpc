#simple-rpc
---

自己实现的一个rpc框架，主要为学习框架中的主要角色以及如何利用动态代理和反射机制实现远程服务的调用。

---
##角色

LoginService - 服务端提供的服务
Importer - 调用远程服务的客户端
Exporter - 提供调用服务的服务端

---
##解析

###测试代码
```java
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
```
