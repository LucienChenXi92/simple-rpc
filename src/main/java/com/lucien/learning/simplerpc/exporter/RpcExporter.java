package com.lucien.learning.simplerpc.exporter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RpcExporter {

    static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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
