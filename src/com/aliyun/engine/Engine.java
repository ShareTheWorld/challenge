package com.aliyun.engine;


import com.aliyun.Main;
import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
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

    private OutputStream out0;
    private OutputStream out1;

    @Override
    public void handleTcpSocket(Socket socket, int port) throws Exception {
        if (port == Main.FILTER_0_PORT) {
            System.out.println(Main.FILTER_0_PORT + " connect success");
            out0 = socket.getOutputStream();
        } else if (port == Main.FILTER_1_PORT) {
            System.out.println(Main.FILTER_1_PORT + " connect success");
            out1 = socket.getOutputStream();
        }
        handleInputStream(socket.getInputStream());
    }


    @Override
    public void handlePacket(Packet packet) {
        if (packet.getType() == Packet.TYPE_MULTI_TRACE_ID) {
            if (packet.getWho() == Packet.WHO_FILTER_0) {
                sendPacket(packet, out1);
            } else {
                sendPacket(packet, out0);
            }
        }
        System.out.println(packet);
    }


    @Override
    protected void setDataPort(int dataPort) {
        System.out.println("engine get port is " + dataPort);
        resultReportPort = dataPort;
        Packet packet = new Packet(1, Main.who, Packet.TYPE_START);
        sendPacket(packet, out0);
        sendPacket(packet, out1);
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
