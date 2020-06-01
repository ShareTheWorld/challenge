package com.aliyun.engine;


import com.aliyun.Main;
import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务器，监听8002端口，
 * 告诉测评程序已经准备好了
 * 获取测评程序设置的端口
 * <p>
 * 转发8000的traceId到8001，转发8001的traceId到8000
 * 接受上报的traceId错误日志
 */
public class Engine extends Server {
    private int resultReportPort;//结果上报端口
    private DatagramSocket socket;
    private InetAddress address;

    private List<Packet> list = new ArrayList<>(100);

    public Engine(int port) {
        super(port);
        try {
            address = InetAddress.getByName("127.0.0.1");
            socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void setListenPort(int listenPort) {
        System.out.println("engine get port is " + listenPort);
        resultReportPort = listenPort;
        Packet packet = new Packet(1, Main.who, Packet.TYPE_START);
        sendPacket(packet, 8000);
        sendPacket(packet, 8001);
    }

    public void sendPacket(Packet packet, int port) {
        try {
            socket.send(packet.getDatagramPacketForRead(address, port));
//            System.out.println("send  " + packet.bs[0] + "  " + packet.bs[1] + "  " + new String(packet.bs, 2, packet.len - 2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int n = 0;

    @Override
    protected void handlePacket(Packet packet) throws Exception {
        if (packet.bs[1] == Packet.TYPE_MULTI_TRACE_ID) {
            System.out.println(packet.bs[0] + "  " + packet.bs[1] + "  " + new String(packet.bs, 2, packet.len - 2));

            if (packet.bs[0] == Packet.WHO_FILTER_0) {
                socket.send(packet.getDatagramPacketForRead(address, 8001));
            } else if (packet.bs[0] == Packet.WHO_FILTER_1) {
                socket.send(packet.getDatagramPacketForRead(address, 8000));
            }
        } else if (packet.bs[1] == Packet.TYPE_MULTI_LOG) {
            System.out.println("n=" + (++n));
            System.out.print(packet);
        }
    }

}
