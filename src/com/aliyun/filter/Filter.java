package com.aliyun.filter;


import com.aliyun.Main;
import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 服务器，监听8000/80001端口，
 * 告诉测评程序已经准备好了
 * 根据测评程序设置的端口，启动数据处理任务
 */
public class Filter extends Server {
    private int dataPort;

    private static Filter filter;

    private Socket socket;
    private OutputStream out;
    private Packet remoteErrorPacket;


    public static Filter getFilter() {
        return filter;
    }

    public Filter(int port) {
        super(port);
        filter = this;
        this.startClient();
    }

    public synchronized void setRemoteErrorPacket(Packet packet) {
        try {
            while (remoteErrorPacket != null) {
                this.wait();
            }
            this.remoteErrorPacket = packet;
            this.notify();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("filter packet error");
        }
    }

    public synchronized Packet getRemoteErrorPacket() {
        try {
            while (remoteErrorPacket == null) {
                this.wait();
            }
            Packet packet = remoteErrorPacket;
            remoteErrorPacket = null;
            notify();
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("filter packet error");
        }

    }


    public void startClient() {
        while (out == null) {
            try {
                InetSocketAddress addr = new InetSocketAddress("127.0.0.1", Main.listenPort);
                socket = new Socket();
                socket.setReuseAddress(true);//端口复用
                socket.bind(addr);//绑定到本定的某个端口上，
                socket.connect(new InetSocketAddress("127.0.0.1", Main.ENGINE_PORT));
                System.out.println("connect to engine success");
                handleInputStream(socket.getInputStream());
                out = socket.getOutputStream();
            } catch (Exception e) {
//                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void handleTcpSocket(Socket socket, int port) throws Exception {
        System.out.println("not tcp connection in filter node ， it only have http! ");
    }

    @Override
    public void handlePacket(Packet packet) {
        if (packet.getType() == Packet.TYPE_MULTI_TRACE_ID) {//filter只会接收到这类packet
            System.out.println("receive multi trace id");
            System.out.println(packet);
            setRemoteErrorPacket(packet);
//                Data.getData().handleErrorTraceId(packet);
        } else {
            System.out.println(packet);
        }
    }


    @Override
    protected void setDataPort(int dataPort) {
        System.out.println("filter get data port is :" + dataPort);
        this.dataPort = dataPort;
        Data.getData().start(dataPort);
    }


    public void sendPacket(Packet packet) {
        try {
//            if (packet.getLen() == 21)
//            System.out.print(packet);
            out.write(packet.getBs(), 0, packet.getLen());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
