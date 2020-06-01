package com.aliyun.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
        boolean goOn = true;
        while (goOn) {
            Socket socket = server.accept();
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
                    setListenPort(port);
                    out.write("HTTP/1.1 200 OK\r\n\r\nsuc".getBytes());
                    goOn = false;
                }
            }
            in.close();
            out.close();
            socket.close();
        }
        server.close();

        goOn();

    }

    private void goOn() {
        try {
            System.out.println("start udp listener port " + (listenPort));
            DatagramSocket socket = new DatagramSocket(listenPort);
            Packet packet = new Packet(32);
            DatagramPacket datagramPacket = packet.getDatagramPacketForWrite();
            while (true) {
                socket.receive(datagramPacket);                                    //接货,接收数据
                packet.len = datagramPacket.getLength();
                handlePacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void handlePacket(Packet packet) throws Exception;

    protected abstract void setListenPort(int listenPort);


}
