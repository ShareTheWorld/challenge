package com.aliyun.engine;


import com.aliyun.Main;
import com.aliyun.common.MD5;
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
    private MD5 md5 = new MD5();

    //总共20000个， "traceId[16]":"md5[32]", 20000 * (1 + 16 + 3 + 32 + 2)
    private byte[] request = new byte[100 * 1024];//100K
    private int requestLen = 0;

    //错误的数据差不多有两万个
    private Map<Packet, Packet> map = new HashMap<>(20000);


    //错误统计
    public static int emptyLogs = 0;
    public static int fullLogs = 0;


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
            synchronized (this) {
//            calcCheckSum(packet);
                if (packet.getLen() == 21) {
                    emptyLogs++;
                System.out.println(packet);
                } else {
//                System.out.println(packet);
                    fullLogs++;
                }
            }
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
        mergeAndMd5(p, packet);
    }

    private void mergeAndMd5(Packet p1, Packet p2) {
        byte bs1[] = p1.getBs();
        int len1 = p1.getLen();
        byte bs2[] = p2.getBs();
        int len2 = p2.getLen();
        int i = Packet.P_DATA, j = Packet.P_DATA;
        //计算时间偏移量
        int offset = bs1[Packet.P_DATA + 2 + 17] == '|' ? 2 + 16 : 2 + 15;
        md5.reset();
        int dataLen1 = 0, dataLen2 = 0;
        while (i < len1 || j < len2) {
            try {
                if (i < len1) dataLen1 = ((bs1[i] & 0XFF) << 8) + (bs1[i + 1] & 0XFF);
                if (j < len1) dataLen2 = ((bs2[j] & 0XFF) << 8) + (bs2[j + 1] & 0XFF);
                //选择 bs1
                if (j > len1 || compareBytes(bs1, i + offset, bs2, j + offset, 16) < 0) {
                    md5.update(bs1, i + 2, dataLen1);
                    i += dataLen1 + 2;
                } else {
                    md5.update(bs2, j + 2, dataLen2);
                    j += dataLen2 + 2;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        byte res[] = new byte[32];
        md5.digest(res, 0);
        System.out.print(new String(bs1, Packet.P_DATA + 2, offset - 2) + "   " + new String(res));
    }

    private int compareBytes(byte bs1[], int s1, byte bs2[], int s2, int len) {
        for (int i = 0; i < 16; i++) {//TODO 时间的前面几位数可以不比较
            if (bs1[s1 + i] == bs2[s2 + i]) continue;
            return bs1[s1 + i] - bs2[s2 + i];
        }
        return 0;
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
