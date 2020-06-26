package com.aliyun.filter;

import com.aliyun.common.Packet;

public class Container {
    private static Buffer[] bufs = new Buffer[]{new Buffer(), new Buffer()};

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
        select(buf.errPkt);
    }

    static int per = 0;

    public static void select(Packet errPkt) {
        boolean isFinishSelect = false;
        synchronized (Container.class) {
            per++;
            while (bufs[0].status != 1 || bufs[1].status != 1) {//如果buffer中有一个没有准备好都需要等待
                try {
                    Container.class.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (per >= 4 && per % 2 == 0) isFinishSelect = true;
        }
        int page = errPkt.getPage();
        Buffer buf = bufs[page % bufs.length];//只查询自己所在的那一页，实际场景中前后页都要查询
        buf.select(errPkt);

        synchronized (Container.class) {
            if (isFinishSelect) {
                Container.class.notifyAll();
                if (per <= 4) bufs[0].status = 0;
                else bufs[(per - 1) % 2].status = 1;
            }
        }
    }


}
