package com.aliyun.filter;

import com.aliyun.common.Packet;

import static com.aliyun.common.Const.*;

/**
 * Page容器，主要负责管理Page
 */
public class Container {
    private static final int len = 12;
    private static final Page[] emptyPages = new Page[len];//空的页
    private static final Page[] fullPages = new Page[len];//读满了数据的页
    private static final Page[] handlePages = new Page[len];//处理完索引和错误的页

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

    public static synchronized Page getHandlePage(int i) {
        if (i >= total_page_count) return null;
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
//            System.out.println("move empty to full, page=" + i);
//            printPage();
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
//            System.out.println("move full to handle, page=" + i);
//            printPage();
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static synchronized void moveHandleToEmpty(int i) {
        try {
            emptyPages[i % len] = handlePages[i % len];
            handlePages[i % len] = null;
//            System.out.println("move handle to empty, page=" + i);
//            printPage();
            Container.class.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void handleErrorPacket() {
        int pageIndex = 0;
        Page page;
        while (pageIndex < total_page_count) {
            System.out.println("handle error, page=" + pageIndex + ", totalPageCount=" + total_page_count);
            //处理本本地的错误
            for (int i = 0; i < PER_HANDLE_PAGE_NUM + 1; i++) {
                page = getHandlePage(pageIndex + i);
                if (page == null) break;
                page.select(page.errPkt);
            }

            //处理远程的错误
            for (int i = 0; i < PER_HANDLE_PAGE_NUM; i++) {
                Packet packet = filter.getPacket(pageIndex + i);
                if (packet == null) break;
                page = getHandlePage(pageIndex + i);
                page.select(packet);
            }

            //移动对应的page到empty中去
            for (int i = 0; i < PER_HANDLE_PAGE_NUM; i++) {
                if (pageIndex + i - PER_HANDLE_PAGE_NUM >= 0) {
                    moveHandleToEmpty(pageIndex + i - PER_HANDLE_PAGE_NUM);
                }
            }
            pageIndex += PER_HANDLE_PAGE_NUM;
        }

        Packet endPacket = new Packet(1, who, Packet.TYPE_END);
        filter.sendPacket(endPacket);
        System.out.println("----------end----------end----------end----------end----------");
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
