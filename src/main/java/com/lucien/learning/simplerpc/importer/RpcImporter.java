package com.lucien.learning.simplerpc.importer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

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
