package com.aliyun.filter;


import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.net.*;

/**
 * 服务器，监听8000/80001端口，
 * 告诉测评程序已经准备好了
 * 根据测评程序设置的端口，启动数据处理任务
 */
public class Filter extends Server {
    private int dataPort;

    private InetAddress address;
    private static Filter filter;
    private DatagramSocket socket;


    public static Filter getFilter() {
        return filter;
    }

    public Filter(int port) throws UnknownHostException {
        super(port);
        try {
            filter = this;
            address = InetAddress.getByName("127.0.0.1");
            socket = new DatagramSocket();                //创建Socket相当于创建码头
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTcpSocket(Socket socket, int port) throws Exception {

    }


    @Override
    protected void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }

//    @Override
//    protected void handlePacket(Packet packet) {
//        if (packet.bs[1] == Packet.TYPE_START) {
//            Data.getData().start(dataPort);
//        }
//        System.out.println("receive " + packet.bs[0] + "  " + packet.bs[1] + "  " + new String(packet.bs, 2, packet.len - 2));
//
//    }

    public void sendPacket(Packet packet) {

    }


}
