package com.aliyun.engine;


import com.aliyun.Main;
import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

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


    //总共20000个， "traceId[16]":"md5[32]", 20000 * (1 + 16 + 3 + 32 + 2)
    private byte[] request = new byte[100 * 1024];//100K
    private int requestLen = 0;

    //错误的数据差不多有两万个
    private Map<Packet, Packet> map = new HashMap<>(20000);


    public Engine(int port) {
        super(port);
        try {
            byte bs[] = "".getBytes();
            System.arraycopy(bs, 0, request, 0, bs.length);
            requestLen = bs.length;
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
                System.out.println("send multi trace id to filter 1");
                sendPacket(packet, out1);
            } else {
                System.out.println("send multi trace id to filter 0");
                sendPacket(packet, out0);
            }
            System.out.println(packet);

        } else if (packet.getType() == Packet.TYPE_MULTI_LOG) {
            calcCheckSum(packet);
//            System.out.println(packet);
        }
    }

    private void calcCheckSum(Packet packet) {
        Packet p = map.get(packet);
        if (p == null) {
            map.put(packet, packet);
            return;
        }
        if (p.getWho() == packet.getWho()) return;//如果是来自同一个Filter

        //使用归并排序对两个数据进行处理,并计算校验和，放到request中去

    }


    @Override
    protected void setDataPort(int dataPort) {
        System.out.println("engine get port is " + dataPort);
        resultReportPort = dataPort;
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
