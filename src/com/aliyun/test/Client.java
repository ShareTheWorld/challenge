package com.aliyun.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    public static void main(String args[]) throws Exception {
        DatagramSocket socket = new DatagramSocket();                //创建Socket相当于创建码头
        socket.setSendBufferSize(32 * 1024 * 1024);
        for (int i = 0; i < 100000; i++) {
            String str = "what are you 弄啥呢?";
            DatagramPacket packet = new DatagramPacket(str.getBytes(), str.getBytes().length, InetAddress.getByName("127.0.0.1"), 8000);
            socket.send(packet);
        }
        //发货,将数据发出去
//        socket.receive(packet);
//        socket.close();
    }
}
