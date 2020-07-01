package com.aliyun.test;

import com.aliyun.common.Utils;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class DMADemo {
    public static ByteBuffer allocateDirect(int capacity) {
//        return ByteBuffer.allocateDirect(capacity);
        return ByteBuffer.allocate(capacity);
//        return new DirectByteBuffer(capacity);
    }

    static String request = ("GET " + "/trace1.data" + " HTTP/1.0\r\n" +
            "Host: localhost:" + 0 + "\r\n" +
            "\r\n");

    public static void main(String args[]) throws Exception {

        ByteBuffer bb = allocateDirect(256 * 1024);
//         创建选择器
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);
        socketChannel.connect(new InetSocketAddress("localhost", 7000));
        socketChannel.write(ByteBuffer.wrap(request.getBytes()));
        int n = 0;
        long time = System.currentTimeMillis();
        while (socketChannel.read(bb) != 0) ;
        int tailLen = 0;
        int len = bb.capacity();
        byte tail[] = new byte[1024];
        //反向找到换行符
        for (tailLen = 0; tailLen < 1024; tailLen++) {
            if (bb.get(len - 1 - tailLen) == '\n') {
                Utils.arraycopy(bb, len - tailLen, tail, 0, tailLen);
                break;
            }
        }

        System.out.println(System.currentTimeMillis() - time);

//        while (true) {
//            int selectInt = selector.select();
//            if (selectInt == 0)
//                continue;
//
//            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
//            while (iterator.hasNext()) {
//                SelectionKey key = iterator.next();
//                if (key.isConnectable()) {
//                    handleConnect(key);
//                }
//                if (key.isReadable()) {
//                    handleRead(key);
//                }
//                if (key.isWritable()) {
//                    handleWrite(key);
//                }
//                iterator.remove();
//            }
//        }
    }

    public static void handleConnect(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(key.selector(), SelectionKey.OP_READ);

        sendInfo(channel, request);
    }

    public static void handleRead(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        String msg = "";
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (channel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                msg += new String(buffer.get(new byte[buffer.limit()]).array());
            }
            buffer.clear();
        }

        System.out.println("收到服务端消息:" + msg);


    }

    public static void handleWrite(SelectionKey key) throws Exception {
        System.out.println("客户端写数据!");
    }

    public static void sendInfo(SocketChannel clientChannel, String msg) throws Exception {
        // 向服务端发送连接成功信息
        clientChannel.write(ByteBuffer.wrap(msg.getBytes()));
    }
}
