package com.aliyun.filter;


import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.aliyun.common.Const.*;

/**
 * 服务器，监听8000/80001端口，
 * 告诉测评程序已经准备好了
 * 根据测评程序设置的端口，启动数据处理任务
 */
public class Filter extends Server {
    protected ServerSocket server;
    private Socket socket;
    private OutputStream out;
    private static int remotePageCount = 10000;
    private Packet remoteErrorPacket;

    public synchronized void setRemoteErrorPacket(Packet packet) {
        try {
            while (remoteErrorPacket != null) {
                this.wait();
            }
            this.remoteErrorPacket = packet;
            this.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("filter packet error");
        }
    }

    public synchronized Packet getRemoteErrorPacket() {
        System.out.println("start get remote err pkt");
        try {
            while (remoteErrorPacket == null) {
                this.wait();
            }
            Packet packet = remoteErrorPacket;
            remoteErrorPacket = null;
            notifyAll();
            System.out.println("end get remote err pkt");
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("filter packet error");
        }
    }


    public void start() throws Exception {
        //连接到engine
        Socket s = connectEngine();

        System.out.println("start tcp listener port " + listen_port);
        server = new ServerSocket(listen_port);
        while (data_port == 0) {//拿到端口就可以不用再监听了
            Socket socket = server.accept();
            handleHttpSocket(socket);
        }
        System.out.println("stop tcp listener port " + listen_port);
        handleInputStream(s);
    }


    @Override
    public void handlePacket(Packet packet) {
        if (packet.getType() == Packet.TYPE_MULTI_TRACE_ID) {//filter只会接收到这类packet
            System.out.println(packet);
            setRemoteErrorPacket(packet);
        } else if (packet.getType() == Packet.TYPE_START) {
            new Thread(() -> Data.start()).start();
        } else if (packet.getType() == Packet.TYPE_READ_END) {
            System.out.println(packet);
            remotePageCount = packet.getPage();
        }
    }


    public Socket connectEngine() {
        int i = 0;
        while (out == null) {
            System.out.println("try to connect engine : " + i++);
            try {
                InetSocketAddress addr = new InetSocketAddress("127.0.0.1", listen_port);
                socket = new Socket();
                socket.setReuseAddress(true);//端口复用
                socket.bind(addr);//绑定到本定的某个端口上，
                socket.connect(new InetSocketAddress("127.0.0.1", ENGINE_LISTEN_PORT));
                System.out.println("connect to engine success");
                out = socket.getOutputStream();
                return socket;
            } catch (Exception e) {
//                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public synchronized void sendPacket(Packet packet) {
        try {
            out.write(packet.getBs(), 0, packet.getLen());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
