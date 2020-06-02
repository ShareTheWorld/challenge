package com.aliyun.filter;


import com.aliyun.Main;
import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

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


    public static Filter getFilter() {
        return filter;
    }

    public Filter(int port) throws UnknownHostException {
        super(port);
        filter = this;
    }

    @Override
    public void startFinish() {
        try {
            server.close();
            InetSocketAddress addr = new InetSocketAddress("127.0.0.1", Main.listenPort);
            socket = new Socket();
            socket.setReuseAddress(true);//端口复用
            socket.bind(addr);//绑定到本定的某个端口上，
            socket.connect(new InetSocketAddress("127.0.0.1", Main.ENGINE_PORT));
            handleInputStream(socket.getInputStream());
            out = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void handleTcpSocket(Socket socket, int port) throws Exception {
        System.out.print("filter not tcp，only have http! ");
    }

    @Override
    public void handlePacket(Packet packet) {
        System.out.print(packet);
    }


    @Override
    protected void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }


    public void sendPacket(Packet packet) {

    }


}
