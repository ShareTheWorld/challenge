package com.aliyun.filter;


import com.aliyun.common.Packet;
import com.aliyun.common.Utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static com.aliyun.common.Const.*;


/**
 * 数据处理线程
 * 从网络中获取数据，并按照页存放
 * 为每一页建立索引，并且造出错误
 * 根据traceId查询出错误的日志
 */
public class Data {
    public static final int PER_READ_LEN = 366 * 1024;//每次读取长度
    private static long startTime;
    //用于存放错误的日志

    private static byte[] tail = new byte[1024];//尾巴数据
    private static int tailLen = 0;//尾巴数据的长度
    private static int pageIndex;//表示多少页


    public static void start() {
        try {
            start0();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 负责读取数据
     */
    public static void start0() throws Exception {
        startTime = System.currentTimeMillis();

//        String path = "http://127.0.0.1:" + data_port + (listen_port == 8000 ? "/trace1.data" : "/trace2.data");
//        System.out.println(path);
//        URL url = new URL(path);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
//        InputStream in = conn.getInputStream();

        String path = "http://127.0.0.1:" + data_port + (listen_port == 8000 ? "/trace1.data" : "/trace2.data");
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);
        socketChannel.connect(new InetSocketAddress("localhost", data_port));
        String request = ("GET " + path + " HTTP/1.0\r\n" +
                "Host: localhost:" + 0 + "\r\n" +
                "\r\n");
        socketChannel.write(ByteBuffer.wrap(request.getBytes()));

        int n, len;
        ByteBuffer data;
        Page page;
        do {
            long l = System.currentTimeMillis();
            //获取一个buf
            page = Container.getEmptyPage(pageIndex);
            page.clear();
            page.pageIndex = pageIndex;

            //将尾巴复制到缓冲区中
            data = page.data;
            data.put(tail, 0, tailLen);

            //读取一页数据，
            while ((n = socketChannel.read(data)) > 0) ;
            data.flip();

            len = data.limit();

            //反向找到换行符
            for (tailLen = 0; tailLen < 1024; tailLen++) {
                try {
                    if (data.get(len - 1 - tailLen) == '\n') {
                        Utils.arraycopy(data, len - tailLen, tail, 0, tailLen);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Container.moveEmptyToFull(pageIndex);
            //计算长度
            page.len = len - tailLen;

            Container.asyncHandleData(pageIndex);
            pageIndex++;
            System.out.println("read data, page=" + pageIndex + ",time=" + (System.currentTimeMillis() - l));

        } while (n != -1);
        total_page_count = pageIndex;
        Packet readEndPacket = new Packet(1, who, Packet.TYPE_READ_END);
        readEndPacket.writePage(pageIndex);
        filter.sendPacket(readEndPacket);

        System.out.println("read data total time=" + System.currentTimeMillis() + " - " + startTime + "=" + (System.currentTimeMillis() - startTime));

    }


}
