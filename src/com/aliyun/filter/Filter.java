package com.aliyun.filter;

import com.aliyun.common.Packet;
import com.aliyun.common.Server;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.aliyun.common.Const.*;

public class Filter extends Server {
    protected ServerSocket server;
    private Socket socket;
    private OutputStream out;
    private Packet remoteErrorPacket;


    public void start() throws Exception {
        Socket s = connectEngine();
        System.out.println("start tcp listener port " + listen_port);
        server = new ServerSocket(listen_port);
        while (data_port == 0) {//拿到端口就可以不用再监听了
            Socket socket = server.accept();
            handleHttpSocket(socket);
        }
        System.out.println("stop tcp listener port " + listen_port);
        handleInputStream(s);
    }


    public void handleTcpSocket(Socket socket, int port) throws Exception {
        System.out.println("not tcp connection in filter node ， it only have http! ");
    }
    

    public void handlePacket(Packet packet) {
        if (packet.getType() == Packet.TYPE_MULTI_TRACE_ID) {//filter只会接收到这类packet
//            setRemoteErrorPacket(packet);
        } else if (packet.getType() == Packet.TYPE_START) {//启动数据处理接口
            System.out.println("receive start packet");
            try {
                Data.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(packet);
        }
    }

    public Socket connectEngine() {
        int i = 0;
        while (out == null) {
            System.out.println("try to connect engine : " + i++);
            try {
                InetSocketAddress addr = new InetSocketAddress("127.0.0.1", listen_port);
                socket = new Socket();
                socket.setReuseAddress(true);//端口复用
                socket.bind(addr);//绑定到本定的某个端口上，
                socket.connect(new InetSocketAddress("127.0.0.1", ENGINE_LISTEN_PORT));
                System.out.println("connect to engine success");
                out = socket.getOutputStream();
                return socket;
            } catch (Exception e) {
//                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void sendPacket(Packet packet) {
        try {
            out.write(packet.getBs(), 0, packet.getLen());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
