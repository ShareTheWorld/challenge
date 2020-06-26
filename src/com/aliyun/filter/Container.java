package com.aliyun.filter;

import com.aliyun.common.Packet;

public class Container {
    private static Buffer[] bufs = new Buffer[]{new Buffer(), new Buffer()};

    static {
        bufs[1].status = 1;
    }


    public static synchronized Buffer get(int page) {
        Buffer buf = bufs[page % bufs.length];
        while (buf.status != 0) {
            try {
                Container.class.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        buf.status = 1;
        return buf;
    }

    public static synchronized void finishIndex(Buffer buf) {
        buf.status = 1;
        Container.class.notifyAll();
        System.out.println("select error logs at local, page=" + buf.page);
        select(buf.errPkt);
    }

    static int selectCount = 0;//查询次数

    public static void select(Packet errPkt) {
        boolean isFinishSelect = false;
        synchronized (Container.class) {
            selectCount++;
            while (bufs[0].status != 1 || bufs[1].status != 1) {//如果buffer中有一个没有准备好都需要等待
                try {
                    Container.class.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (selectCount % 2 == 0) isFinishSelect = true;
        }
        int page = errPkt.getPage();
        Buffer buf = bufs[page % bufs.length];//只查询自己所在的那一页，实际场景中前后页都要查询
        buf.select(errPkt);

        synchronized (Container.class) {
            if (isFinishSelect) {
                bufs[(selectCount >> 1) % 2].status = 0;
                Container.class.notifyAll();
            }
        }
    }


}
