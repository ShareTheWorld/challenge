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
    private static final int len = 11;//表示读取数据的缓存大小,最后一个会作为缓存卡着
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
