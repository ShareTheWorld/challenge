package com.aliyun.filter.socket;


import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {

    public static void main(String args[]) {
        System.out.println(Arrays.toString(args));
        new Thread(() -> {
            new Server(8000);
        }).start();
        new Thread(() -> {
            new Server(8001);
        }).start();
        new Thread(() -> {
            new Server(8002);
        }).start();
        new Thread(() -> {
            new Server(8003);
        }).start();
    }

    public Server(int port) {
        try {
            init(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(int port) throws Exception {
        ServerSocket server = new ServerSocket(port);
        while (true) {
            Socket socket = server.accept();
            new SocketHandle(socket);
        }

    }
}
