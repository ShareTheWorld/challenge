package com.aliyun.common;

import com.aliyun.Main;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Server {
    protected ServerSocket server;
    protected int listenPort;//代表的是这个服务需要监听的端口，主要是有启动的时候的参数控制

    public Server(int listenPort) {
        this.listenPort = listenPort;
    }

    public void run() throws Exception {
        System.out.println("start tcp listener port " + listenPort);
        server = new ServerSocket(listenPort);
        while (!server.isClosed()) {
            Socket socket = server.accept();
            int port = socket.getPort();
            if (port == Main.FILTER_0_PORT || port == Main.FILTER_1_PORT) {
                handleTcpSocket(socket, port);
            } else {
                handleHttpSocket(socket);
            }
        }
        System.out.println("stop tcp listener port " + listenPort);
    }

    private void handleHttpSocket(Socket socket) throws Exception {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        byte bs[] = new byte[1024];
        int len = in.read(bs);
        if (len > 0) {
            String req = new String(bs, 0, len);
            if (req.contains("ready")) {
                System.out.println("call api: ready");
                out.write("HTTP/1.1 200 OK\r\n\r\nsuc".getBytes());
            }
            if (req.contains("setParameter")) {
                System.out.println("call api: setParameter");
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

    public void handleInputStream(InputStream in) {
        new Thread(() -> {
            try {
                while (true) {
                    byte bs[] = new byte[3];
                    read(in, bs, 0, 3);
                    int totalLen = ((bs[0] & 0XFF) << 16) + ((bs[1] & 0XFF) << 8) + (bs[2] & 0XFF);
                    byte data[] = new byte[totalLen];
                    data[0] = bs[0];
                    data[1] = bs[1];
                    data[2] = bs[2];
                    read(in, data, 3, totalLen - 3);
                    Packet packet = new Packet(data, data.length);
                    handlePacket(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //从in中读取n个数据，写到bs中，从s位置开始些
    private void read(InputStream in, byte[] bs, int s, int n) throws Exception {
        int len;
        while ((len = in.read(bs, s, n)) != -1) {
            if (n - len == 0) break;
            s += len;
            n -= len;
        }

    }

    public abstract void handlePacket(Packet packet);

    protected abstract void setDataPort(int dataPort);


}
