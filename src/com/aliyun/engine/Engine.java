package com.aliyun.engine;

import com.aliyun.common.Server;

import java.net.Socket;

public class Engine extends Server {
    @Override
    public void handleTcpSocket(Socket socket, int port) throws Exception {

    }

    @Override
    protected void setDataPort(int dataPort) {
        
    }
}
