# simple-rpc

自己实现的一个rpc框架，主要为学习框架中的主要角色以及如何利用动态代理和反射机制实现远程服务的调用。

---
## 角色
LoginService - 服务端提供的服务 

Importer - 调用远程服务的客户端

Exporter - 提供调用服务的服务端

---
## 解析

### 测试代码
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

### Exporter
```java
public class RpcExporter {

    // 线程池
    static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // 绑定endpoint和端口，持续监听
    public static void exporter(String hostName, int port) throws Exception {
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(hostName, port));
        try {
            while(true) {
                executor.execute(new ExporterTask(server.accept()));
            }
        }
        finally {
            server.close();
        }
    }

    private static class ExporterTask implements Runnable {

        Socket client = null;

        public ExporterTask(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;

            try {
                input = new ObjectInputStream(client.getInputStream());
                // 获取目标接口类名
                String interfaceName = input.readUTF();
                // 获取目标接口Class对象
                Class<?> service = Class.forName(interfaceName);
                // 获取方法名
                String methodName = input.readUTF();
                // 获取参数类型
                Class<?>[] parameterTypes = (Class<?>[])input.readObject();
                // 获取参数变量
                Object[] arguments = (Object[])input.readObject();
                // 获取方法对象
                Method method = service.getMethod(methodName, parameterTypes);
                // 执行目标方法
                Object result = method.invoke(service.newInstance(), arguments);
                // 返回执行结果
                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                // 关流
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
```

### Importer
```
public class RpcImporter<S> {
    public S importer(final Class<?> serviceClass, final InetSocketAddress addr) {
        return (S) Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class<?>[] {serviceClass.getInterfaces()[0]}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = null;
                ObjectOutputStream output = null;
                ObjectInputStream input = null;
                try {
                    // 构建连接
                    socket = new Socket();
                    socket.connect(addr);
                    output = new ObjectOutputStream(socket.getOutputStream());
                    // 提供类名
                    output.writeUTF(serviceClass.getName());
                    // 提供方法名
                    output.writeUTF(method.getName());
                    // 提供参数类型
                    output.writeObject(method.getParameterTypes());
                    // 提供参数变量
                    output.writeObject(args);
                    // 读取服务提供端执行结果
                    input = new ObjectInputStream(socket.getInputStream());
                    return input.readObject();
                }
                // 关流
                finally {
                    if (socket != null) {
                        socket.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        output.close();
                    }
                }
            }
        });
    }
}
```
