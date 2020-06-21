package com.aliyun.filter;

import com.aliyun.common.Server;

import java.net.Socket;

import static com.aliyun.common.Const.*;

public class Filter extends Server {


    @Override
    public void handleTcpSocket(Socket socket, int port) throws Exception {

    }

    @Override
    protected void setDataPort(int dataPort) {
        data_port = dataPort;
        try {
            Data.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
