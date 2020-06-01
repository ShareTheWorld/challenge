package com.aliyun.common;

import com.aliyun.Main;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Server {
    protected int listenPort;//代表的是这个服务需要监听的端口，主要是有启动的时候的参数控制

    public Server(int listenPort) {
        this.listenPort = listenPort;
    }

    public void run() throws Exception {
        System.out.println("start tcp listener port " + listenPort);
        ServerSocket server = new ServerSocket(listenPort);
        while (true) {
            Socket socket = server.accept();
            int port = socket.getPort();
            if (port == Main.FILTER_0_PORT || port == Main.FILTER_1_PORT) {
                handleTcpSocket(socket, port);
            } else {
                handleHttpSocket(socket);
            }

        }


    }

    private void handleHttpSocket(Socket socket) throws Exception {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        byte bs[] = new byte[1024];
        int len = in.read(bs);
        if (len > 0) {
            String req = new String(bs, 0, len);
            if (req.contains("ready")) {
                out.write("HTTP/1.1 200 OK\r\n\r\nsuc".getBytes());
            }
            if (req.contains("setParameter")) {
                int s = req.indexOf('=');
                int e = req.indexOf(' ', s);
                int port = Integer.valueOf(req.substring(s + 1, e));
                setDataPort(port);
                out.write("HTTP/1.1 200 OK\r\n\r\nsuc".getBytes());
            }
        }
        in.close();
        out.close();
        socket.close();
    }




    public abstract void handleTcpSocket(Socket socket, int port) throws Exception;


    protected abstract void setDataPort(int dataPort);


}
