package com.aliyun.engine;

import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.aliyun.common.Const.*;

public class Engine extends Server {
    private OutputStream out0;
    private OutputStream out1;
    protected ServerSocket server;


    public void start() throws Exception {
        System.out.println("start tcp listener port " + listen_port);
        server = new ServerSocket(listen_port);
        while (data_port == 0) {//拿到了端口就可以不用再监听了
            Socket socket = server.accept();
            int port = socket.getPort();
            if (port == FILTER_0_LISTEN_PORT) {
                System.out.println(FILTER_0_LISTEN_PORT + " connect success");
                out0 = socket.getOutputStream();
                new Thread(() -> handleInputStream(socket)).start();
            } else if (port == FILTER_1_LISTEN_PORT) {
                System.out.println(FILTER_1_LISTEN_PORT + " connect success");
                out1 = socket.getOutputStream();
                new Thread(() -> handleInputStream(socket)).start();
            } else {
                handleHttpSocket(socket);
            }
        }
        System.out.println("stop tcp listener port " + listen_port);

        //当结束监听的时候，说明获取到数据了，通知节点启动
        Packet packet = new Packet(1, who, Packet.TYPE_START);
        sendPacket(packet, out0);
        sendPacket(packet, out1);
    }


    public void handlePacket(Packet packet) {
        if (packet.getType() == Packet.TYPE_MULTI_TRACE_ID) {
//            System.out.println(packet);
        } else {
            System.out.println("not handle packet");
        }
    }


    public void sendPacket(Packet packet, OutputStream out) {
        try {
            int len = packet.getLen();
            byte bs[] = packet.getBs();
            out.write(bs, 0, len);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
