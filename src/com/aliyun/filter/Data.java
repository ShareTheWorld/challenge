package com.aliyun.filter;


import com.aliyun.Main;
import com.aliyun.common.Packet;

import java.io.FileInputStream;
import java.io.InputStream;


/**
 * 数据处理线程
 * 从网络中获取数据，并按照页存放
 * 为每一页建立索引，并且造出错误
 * 根据traceId查询出错误的日志
 */
public class Data implements Runnable {
    public static final int PER_READ_LEN = 256 * 1024;//每次读取长度
    public static Data data = new Data();
    private int dataPort;
    private int totalPageCount = 100000;//表示总页数，当真正的页数被计算出来过后会赋值给他
    public static final int PER_HANDLE_PAGE_NUM = 10;//表示每次处理多少页数据，必须小于读取数据缓存页的长度-1
    private long startTime;
    //用于存放错误的日志
    public static Packet errorPackets[] = new Packet[300 / PER_HANDLE_PAGE_NUM];

    static {
        for (int i = 0; i < errorPackets.length; i++) {
            errorPackets[i] = new Packet(24, Main.who, Packet.TYPE_MULTI_TRACE_ID);
        }
    }


    //统计用
    private int emptyLogs = 0;
    private int fullLogs = 0;

    public static Data getData() {
        return data;
    }

    public Data() {
        new Thread(() -> handleData()).start();
    }


    public void start(int dataPort) {
        System.out.println("start data handle thread");
        this.dataPort = dataPort;
        new Thread(this).start();
    }


    /**
     * 负责读取数据
     */
    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        try {
            String path = "/Users/fht/d_disk/chellenger/data";
//            String path = "/home/fu/Desktop/challege/data";
            InputStream in = new FileInputStream(path + (Main.listenPort == 8000 ? "/trace1.data" : "/trace2.data"));
//            String path = "http://127.0.0.1:" + dataPort + (Main.listenPort == 8000 ? "/trace1.data" : "/trace2.data");
//            System.out.println(path);
//            URL url = new URL(path);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
//            InputStream in = conn.getInputStream();

            int pageIndex = 0;
            Page page = Container.getEmptyPage(pageIndex);
            long totalCount = 0;
            synchronized (Data.class) {
                while (true) {
                    System.out.println("start get a page data,pageIndex=" + pageIndex);

                    //读取一页数据，
                    int len;
                    while ((len = in.read(page.data, page.len, Math.min(PER_READ_LEN, page.data.length - page.len))) != -1) {
                        page.len += len;
                        if (page.len == Page.min) break;
                    }

                    //将尾部数据放到下一页
                    Page newPage = Container.getEmptyPage(pageIndex + 1);//添加一个新的page
                    newPage.clear();
                    for (int i = page.len - 1; i >= 0; i--) {
                        if ('\n' == page.data[i]) {
                            int l = page.len - i - 1;
                            System.arraycopy(page.data, i + 1, newPage.data, 0, l);
                            page.len = page.len - l;
                            newPage.len = l;
                            break;
                        }
                    }
                    Container.movePageToFull(pageIndex);
                    totalCount += page.len;
                    page = newPage;
                    pageIndex++;
                    if (len == -1) break;
                }
            }
            totalPageCount = pageIndex;
            System.out.println("totalCount=" + totalCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("read data total time=" + (System.currentTimeMillis() - startTime));
    }


    /**
     * 负责处理数据
     */
    public void handleData() {
        int pageIndex = 0;
        while (pageIndex < totalPageCount) {
            //创建一个Packet，用于存放错误
            Packet packet = errorPackets[pageIndex / PER_HANDLE_PAGE_NUM];//
            int i = 0;
//            Packet packet = new Packet(24, Main.who, Packet.TYPE_MULTI_TRACE_ID);
            for (; i < PER_HANDLE_PAGE_NUM && pageIndex < totalPageCount; i++) {//表示每次处理多少页
                Page page = Container.getFullPage(pageIndex++);
//                page.errorPacket = packet;
                page.createIndex();
            }
            //TODO 处理数据
            Filter.getFilter().sendPacket(packet);//发送错traceIds到engine
            handleErrorTraceId(pageIndex - i, pageIndex, packet);
            Container.moveAllPageToEmpty(pageIndex - i, pageIndex);
        }

        System.out.println(pageIndex + " handle data total time=" + (System.currentTimeMillis() - startTime));
        System.out.println("emptyLogs=" + emptyLogs + ", fullLogs=" + fullLogs);
    }

    private void handleErrorTraceId(int start, int end, Packet packet) {
        //处理本地错误traceId
        System.out.println("select by local trace id ,from [" + start + "," + end + ")");
        realHandleErrorTraceId(start, end, packet);

        //处理其他节点发送过来的traceId
        packet = Filter.getFilter().getRemoteErrorPacket();
        System.out.println("select by remote trace id ,from [" + start + "," + end + ")");
        realHandleErrorTraceId(start, end, packet);


    }

    private void realHandleErrorTraceId(int start, int end, Packet packet) {
        System.out.println("traceIds number=" + (packet.getLen() - Packet.P_DATA) / 16);
        //处理本地错误traceId
        byte[] bs = packet.getBs();
        for (int i = Packet.P_DATA; i < packet.getLen(); i += 16) {
            byte traceId[] = new byte[16];
            System.arraycopy(bs, i, traceId, 0, 16);
            Packet logsPacket = Container.selectByTraceId(start, end, traceId);
            if (logsPacket.getLen() == 21) {
                emptyLogs++;
            } else {
                fullLogs++;
            }
            Filter.getFilter().sendPacket(logsPacket);
        }
    }


}
