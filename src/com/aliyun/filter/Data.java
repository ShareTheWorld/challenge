package com.aliyun.filter;


import com.aliyun.Main;
import com.aliyun.common.Packet;

import javax.xml.crypto.dsig.spec.XPathType;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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


    private Page[] pages = new Page[11];
    private int pageIndex = 0;
    public static Packet localError = new Packet(24, Main.who, Packet.TYPE_MULTI_TRACE_ID);


    public static Data getData() {
        return data;
    }

    public Data() {
        for (int i = 0; i < pages.length; i++) {
            pages[i] = new Page();
        }
    }


    public void start(int dataPort) {
        System.out.println("start data handle thread");
        this.dataPort = dataPort;
        new Thread(this).start();
    }

    private Page getPage() {
        Page page = pages[pageIndex % pages.length];
        pageIndex++;
        return page;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            String path = "/Users/fht/d_disk/chellenger/data";
//            String path = "/home/fu/Desktop/challege/data";
            InputStream in = new FileInputStream(path + (Main.listenPort == 8000 ? "/trace1.data" : "/trace2.data"));
//            String path = "http://127.0.0.1:" + dataPort + (Main.listenPort == 8000 ? "/trace1.data" : "/trace2.data");
//            System.out.println(path);
//            URL url = new URL(path);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
//            InputStream in = conn.getInputStream();

            Page page = getPage();
            long totalCount = 0;
            synchronized (Data.class) {
                while (true) {
                    System.out.println("start get a page data,pageIndex=" + pageIndex);
                    //保证读取的一页接近1M，
                    int len;
                    while ((len = in.read(page.data, page.len, Math.min(PER_READ_LEN, page.data.length - page.len))) != -1) {
                        page.len += len;
                        if (page.len == Page.min) break;
                    }
                    Page newPage = getPage();//添加一个新的page
                    for (int i = page.len - 1; i >= 0; i--) {
                        if ('\n' == page.data[i]) {
                            int l = page.len - i - 1;
                            System.arraycopy(page.data, i + 1, newPage.data, 0, l);
                            page.len = page.len - l;
                            newPage.len = l;
                            break;
                        }
                    }
                    //将这一页码
                    Page t_page = page;
                    boolean isStartDealData = pageIndex % (pages.length - 1) == 0 || len == -1;
                    new Thread(() -> {
                        t_page.createIndex();
                        if (isStartDealData) handleErrorTraceId(localError);
                    }).start();
                    totalCount += page.len;
                    page = newPage;
                    if (isStartDealData) {//保证每次有一页空闲，存放这一次未处理的尾巴数据
                        System.out.println("send multi trace id");
                        Filter.getFilter().sendPacket(localError);
                        System.out.println("stop read");
//                        while (true) {//TODO 需要等待两个handle都处理完了才唤醒
                        Data.class.wait();
                        localError.write(Main.who, Packet.TYPE_MULTI_TRACE_ID);
//                        }
                        System.out.println("start read");
                    }

                    if (len == -1) break;
                }
            }
            System.out.println("totalCount=" + totalCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("total time=" + (System.currentTimeMillis() - startTime));
    }


    /**
     * 处理所有错错误的traceId
     *
     * @return
     */
    public void handleErrorTraceId(Packet packet) {
        if (packet.getType() != Packet.TYPE_MULTI_TRACE_ID) {
            System.out.println("this packet is not multi trace id type");
            return;
        }
        //处理本地错误traceId
        byte[] bs = packet.getBs();
        int len = packet.getLen();
        for (int i = Packet.P_DATA; i < packet.getLen(); i += 16) {
            byte traceId[] = new byte[16];
            System.arraycopy(bs, i, traceId, 0, 16);
            Packet logsPacket = selectByTraceId(traceId);
            System.out.println(logsPacket);
            Filter.getFilter().sendPacket(logsPacket);
        }
        packet.clear();

    }

    /**
     * 第一步：查询
     * 第二步：排序
     * 第三步：封装为packet
     *
     * @param traceId
     * @return
     */
    public Packet selectByTraceId(byte[] traceId) {
        Packet packet = new Packet(32, Main.who, Packet.TYPE_MULTI_LOG);
        //先写入traceId
        packet.write(traceId, 0, traceId.length);

        List<byte[]> list = new ArrayList<>(30);
        for (int i = 0; i < pages.length; i++) {
            List<byte[]> l = pages[i].selectByTraceId(traceId);
            list.addAll(l);
        }
        if (list.size() <= 0) return packet;//表示没有数据

//        Collections.shuffle(list);//测试用
        //排序 TODO 最好使用插入排序
        Collections.sort(list, (bs1, bs2) -> {
            int s1 = 17, s2 = 17;//正常是17这个位置开始是时间
            if (bs1[15] == '|') s1 = 16;
            if (bs2[15] == '|') s2 = 16;
            for (int i = 10; i < 16; i++) {//TODO 时间的前面几位数可以不比较
                if (bs1[s1 + i] == bs2[s2 + i]) continue;
                return bs1[s1 + i] - bs2[s2 + i];
            }
            return 0;
        });

        //将traceId封装为Packet
        for (byte[] bs : list) {
            packet.writeWithDataLen(bs, 0, bs.length);
        }
        return packet;
    }

}
