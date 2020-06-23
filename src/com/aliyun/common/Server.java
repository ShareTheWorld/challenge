package com.aliyun.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.aliyun.common.Const.*;

public abstract class Server {
    protected ServerSocket server;
    private Socket socket;
    private OutputStream out;
    private Packet remoteErrorPacket;
    private boolean listening = true;//继续监听


    protected void handleHttpSocket(Socket socket) throws Exception {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        byte bs[] = new byte[1024];
        int len = in.read(bs);
        if (len > 0) {
            String req = new String(bs, 0, len);
            if (req.contains("ready")) {
                System.out.println("call api: ready, startTime=" + System.currentTimeMillis());
                out.write("HTTP/1.1 200 OK\r\n\r\nsuc".getBytes());
                out.flush();
            }
            if (req.contains("setParameter")) {
                System.out.println("call api: setParameter=" + System.currentTimeMillis());
                int s = req.indexOf('=');
                int e = req.indexOf(' ', s);
                data_port = Integer.valueOf(req.substring(s + 1, e));
                out.write("HTTP/1.1 200 OK\r\n\r\nsuc".getBytes());
                out.flush();
            }
        }
        in.close();
        out.close();
        socket.close();
    }

    protected void handleInputStream(Socket socket) {
        InputStream in = null;
        try {
            in = socket.getInputStream();
            while (true) {
                byte bs[] = new byte[3];
                readN(in, bs, 0, 3);
                int totalLen = ((bs[0] & 0XFF) << 16) + ((bs[1] & 0XFF) << 8) + (bs[2] & 0XFF);
                byte data[] = new byte[totalLen];
                data[0] = bs[0];
                data[1] = bs[1];
                data[2] = bs[2];
                readN(in, data, 3, totalLen - 3);
                Packet packet = new Packet(data, data.length);
                handlePacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(socket.getPort() + " closed ");
            try {
                socket.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //从in中读取n个数据，写到bs中，从s位置开始些
    public static void readN(InputStream in, byte[] bs, int s, int n) throws Exception {
        int len;
        while ((len = in.read(bs, s, n)) != -1) {
            if (n - len == 0) break;
            s += len;
            n -= len;
        }
    }

    public abstract void handlePacket(Packet packet);

}
