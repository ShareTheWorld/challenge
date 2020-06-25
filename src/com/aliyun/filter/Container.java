package com.aliyun.filter;

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
        buf.select(buf.errPkt);
        buf.status = 0;
        Container.class.notifyAll();
    }

    public void select() {

    }
}
