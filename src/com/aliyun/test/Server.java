package com.aliyun.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public Server() {

    }

    public void start() throws Exception {


        DatagramSocket socket = new DatagramSocket(8000);
        socket.setReceiveBufferSize(128 * 1024 * 1024);
        int size = socket.getReceiveBufferSize();
        System.out.print(size);
        byte bs[] = new byte[20 * 1024];
        DatagramPacket packet = new DatagramPacket(bs, bs.length);//创建Packet相当于创建集装箱
        int n = 0;

        while (true) {
            socket.receive(packet);                                    //接货,接收数据
            byte[] arr = packet.getData();                            //获取数据
            int len = packet.getLength();                            //获取有效的字节个数
            String ip = packet.getAddress().getHostAddress();        //获取ip地址
            int port = packet.getPort();                            //获取端口号
            System.out.println((++n) + "   " + ip + "   :" + port + ":" + new String(arr, 0, len));
        }
    }


    public static void main(String args[]) throws Exception {
        new Server().start();

    }
}
