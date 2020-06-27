package com.aliyun.filter;

import com.aliyun.common.Packet;

import static com.aliyun.common.Const.*;

/**
 * Page容器，主要负责管理Page
 */
public class Container {
    private static final int len = 30;
    public static final int PER_HANDLE_PAGE_NUM = 10;//表示每次处理多少页数据，必须小于读取数据缓存页的长度-1

    private static final Page[] emptyPages = new Page[len];//空的页
    private static final Page[] fullPages = new Page[len];//读满了数据的页
    private static final Page[] handlePages = new Page[len];//处理完索引和错误的页
    private static final Packet[] errPkts = new Packet[300 / PER_HANDLE_PAGE_NUM];
    //用于存放错误的日志

    static {
        for (int i = 0; i < emptyPages.length; i++) {
            emptyPages[i] = new Page();
        }
        for (int i = 0; i < errPkts.length; i++) {
            errPkts[i] = new Packet(8, who, Packet.TYPE_MULTI_TRACE_ID);
        }
        System.out.println("init memory finish");
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

    public static synchronized Page getHandlePage(int i) {
        if (i >= total_page_count || i < 0) return null;
        Page page = handlePages[i % len];
        while (page == null) {
            try {
                Container.class.wait();
                page = handlePages[i % len];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return page;
    }


    //empty -> full
    public static synchronized void moveEmptyToFull(int i) {
        try {
            fullPages[i % len] = emptyPages[i % len];
            emptyPages[i % len] = null;
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //full -> handle
    public static synchronized void moveFullToHandle(int i) {
        try {
            handlePages[i % len] = fullPages[i % len];
            fullPages[i % len] = null;
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized void moveHandleToEmpty(int i) {
        try {
            emptyPages[i % len] = handlePages[i % len];
            handlePages[i % len] = null;
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建索引->找出错误->发送错误
     */
    public static void handleData() {
        int pageIndex = 0;
        while (pageIndex < total_page_count) {
            long startTime = System.currentTimeMillis();
            //创建一个Packet，用于存放错误
            Packet packet = errPkts[pageIndex / PER_HANDLE_PAGE_NUM];
            int i = 0;
            for (; i < PER_HANDLE_PAGE_NUM && pageIndex < total_page_count; i++) {//表示每次处理多少页
                Page page = Container.getFullPage(pageIndex);
                page.errPkt = packet;
                page.createIndexAndFindError();
                moveFullToHandle(pageIndex);//创建完索引，就可以将这个移动到其他地方了
                pageIndex++;
            }
            //TODO 处理数据
            filter.sendPacket(packet);//发送错traceIds到engine
//            asyncHandleErrorPacket(pageIndex - i, pageIndex, packet);
//            System.out.println("create index and find error ,from [" + (pageIndex - i) + "," + pageIndex + "),time=" + (System.currentTimeMillis() - startTime));
        }
//        getHandlePage(pageIndex - 1);
        System.out.println("-------------create index and find error Thread end------------------");
    }

    public static void asyncHandleErrorPacket(int start, int end, Packet packet) {
        new Thread(() -> handleErrorPacket(start, end, packet)).start();
    }

    public static void handleErrorPacket() {

        for (int i = 0; i < total_page_count; i += PER_HANDLE_PAGE_NUM) {
            int j = 0;
            for (; j < PER_HANDLE_PAGE_NUM; j++) {
                Page page = getHandlePage(i + j);
                if (page == null) break;
            }
            //每两页处理一次，最后一页就不处理了
            handleErrorPacket(i, i + j, errPkts[i / PER_HANDLE_PAGE_NUM]);
        }
        Packet endPacket = new Packet(1, who, Packet.TYPE_END);
        filter.sendPacket(endPacket);
        System.out.println("-------------query logs Thread end------------------");

    }

    public static void handleErrorPacket(int start, int end, Packet packet) {
        //处理本地错误traceId TODO 第一次会比较耗时，要等待下一个准备好才能查询
        long startTime = System.currentTimeMillis();
//        System.out.println("select by local trace id ,from [" + start + "," + end + ")");
        handelOnePacket(start, end, packet);
        System.out.println("query error 1,from[" + start + "," + end + ")" + ",time=" + (System.currentTimeMillis() - startTime));
        //处理其他节点发送过来的traceId
        packet = filter.getRemoteErrorPacket();
//        System.out.println("select by remote trace id ,from [" + start + "," + end + ")");
        handelOnePacket(start, end, packet);
        System.out.println("query error 2,from[" + start + "," + end + ")" + ",time=" + (System.currentTimeMillis() - startTime));
        for (int i = start; i < end; i++) {
            if (i - PER_HANDLE_PAGE_NUM >= 0)
                moveHandleToEmpty(i - PER_HANDLE_PAGE_NUM);
        }

    }

    public static void handelOnePacket(int start, int end, Packet packet) {
        //packet大概27个
        byte[] bs = packet.getBs();
        int len = packet.getLen();
        for (int i = Packet.P_DATA; i < len; i += 16) {
            byte traceId[] = new byte[16];
            System.arraycopy(bs, i, traceId, 0, 16);
            Packet logsPacket = selectByTraceId(start, end, traceId);
            filter.sendPacket(logsPacket);
        }
    }

    static Packet packet = new Packet(64, who, Packet.TYPE_MULTI_LOG);

    static Log[] logs = new Log[300];
    static int logsLen = 0;

    public static Packet selectByTraceId(int start, int end, byte traceId[]) {
        packet.reset(who, Packet.TYPE_MULTI_LOG);
        packet.writePage(0);
        //先写入traceId
        packet.write(traceId, 0, traceId.length);
        logsLen = 0;
        for (int i = start > 0 ? start - 1 : start; i < end + 1; i++) {
            Page page = getHandlePage(i);
            if (page == null) break;
            int len = page.selectByTraceId(traceId, logs, logsLen);
            logsLen += len;
        }

        for (int i = 1; i < logsLen; i++) {
            for (int j = i; j > 0; j--) {
                if (compare(logs[j], logs[j - 1]) < 0) {
                    Log tmp = logs[j - 1];
                    logs[j - 1] = logs[j];
                    logs[j] = tmp;
                }
            }
        }

        //将traceId封装为Packet
        for (int i = 0; i < logsLen; i++) {
            Log l = logs[i];
            packet.writeWithDataLen(l.d, l.s, l.l);
        }
        return packet;
    }

    public static int compare(Log l1, Log l2) {
        //因为b1和b2的traceId都是一样的，可以随便指定一个开始位置
        for (int i = 20; i < 35; i++) {//20 时间的前面几位数可以不比较,35 j在超过时间之前就表完了
            if (l1.d[l1.s + i] == l2.d[l2.s + i]) continue;
            return l1.d[l1.s + i] - l2.d[l2.s + i];
        }
        return 0;
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
        for (int i = 0; i < len; i++) {
            sb.append((handlePages[i] != null ? "1" : "_") + "\t");
        }
        sb.append("\n");
        System.out.println(sb.toString());
    }
}
