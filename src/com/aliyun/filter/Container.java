package com.aliyun.filter;

import com.aliyun.Main;
import com.aliyun.common.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Page容器，主要负责管理Page
 */
public class Container {
    private static final int len = 41;//表示读取数据的缓存大小,最后一个会作为缓存卡着
    private static final Page[] emptyPages = new Page[len];
    private static final Page[] fullPages = new Page[len];

    static {
        for (int i = 0; i < emptyPages.length; i++) {
            emptyPages[i] = new Page();
        }
    }


    public static synchronized Page getEmptyPage(int i) {
        Page page = emptyPages[i % len];
        while (page == null) {
            try {
                Container.class.wait();
                page = emptyPages[i % len];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        page.pageIndex = i;
        return page;
    }

    public static synchronized Page getFullPage(int i) {
        Page page = fullPages[i % len];
        while (page == null) {
            try {
                Container.class.wait();
                page = fullPages[i % len];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return page;
    }

    public static synchronized void moveAllPageToEmpty(int start, int end) {
        try {
            for (int i = start; i < end; i++) {
//                if (fullPages[i % len] != null)
                emptyPages[i % len] = fullPages[i % len];
                fullPages[i % len] = null;
            }
//            System.out.println("move [" + start + " - " + end + ") to empty");
//            printPage();
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void movePageToFull(int i) {
        try {
            fullPages[i % len] = emptyPages[i % len];
            emptyPages[i % len] = null;
//            System.out.println("move [" + i + "] to full");
//            printPage();
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printPage() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((emptyPages[i] != null ? "1" : "_") + "\t");
        }
        sb.append("\n");
        for (int i = 0; i < len; i++) {
            sb.append((fullPages[i] != null ? "1" : "_") + "\t");
        }
        sb.append("\n");
        System.out.println(sb.toString());
    }


    /**
     * 第一步：查询
     * 第二步：排序
     * 第三步：封装为packet
     *
     * @param traceId
     * @return
     */
    public static Packet selectByTraceId(int start, int end, byte[] traceId) {
        Packet packet = new Packet(32, Main.who, Packet.TYPE_MULTI_LOG);
        //先写入traceId
        packet.write(traceId, 0, traceId.length);

        List<byte[]> list = new ArrayList<>(30);
        for (int i = start; i < end; i++) {
            List<byte[]> l = fullPages[i % len].selectByTraceId(traceId);
            list.addAll(l);
        }

        //表示没有数据,向前向后查询，如果没有读取数据的缓存页，这个功能是无用的
        if (list.size() <= 0) {//去前后查询一次
            int n = 5;
            //往后面查询n个,以前的在emptyPage中放起
            for (int i = start - n < 0 ? 0 : start - n; i < start; i++) {
                if (emptyPages[i % len] == null) break;
                List<byte[]> l = emptyPages[i % len].selectByTraceId(traceId);
                list.addAll(l);
            }
            //往前面查询n个，新的会在fullPage中放起，但是还没建立索引
            for (int i = end; i < end + n; i++) {
                if (fullPages[i % len] == null) break;
                fullPages[i % len].createIndex();
                List<byte[]> l = fullPages[i % len].selectByTraceId(traceId);
                list.addAll(l);
            }
        }

//        Collections.shuffle(list);//测试用
        //排序 TODO 最好使用插入排序
        Collections.sort(list, (bs1, bs2) -> {
            //因为b1和b2的traceId都是一样的，可以随便指定一个开始位置
//            int s1 = 17, s2 = 17;//正常是17这个位置开始是时间
//            if (bs1[15] == '|') s1 = 16;
//            if (bs2[15] == '|') s2 = 16;
            for (int i = 20; i < 35; i++) {//TODO 时间的前面几位数可以不比较
                if (bs1[i] == bs2[i]) continue;
                return bs1[i] - bs2[i];
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
