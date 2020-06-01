package com.aliyun.common;


import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {
    public static final byte TYPE_START = 1;
    public static final byte TYPE_TRACE_ID = 2;//traceId
    public static final byte TYPE_MULTI_TRACE_ID = 3;//traceId traceId traceId
    public static final byte TYPE_LOG = 4;//len log
    public static final byte TYPE_MULTI_LOG = 5;//len log len log len log
    public static final byte TYPE_END = 0;
    public static final byte WHO_FILTER_0 = 0;//8000
    public static final byte WHO_FILTER_1 = 1;//8001
    public static final byte WHO_ENGINE_2 = 2;//8002
    public byte bs[];//[who,type,data]
    public int len;

    public Packet(int k) {
        bs = new byte[k * 1024];
        len = 0;
    }
    

    public Packet(int k, byte who, byte type) {
        bs = new byte[k * 1024];
        this.bs[0] = who;
        this.bs[1] = type;
        this.len = 2;
    }

    public Packet write(byte bs[], int start, int len) {
        System.arraycopy(bs, start, this.bs, this.len, len);
        this.len += len;
        return this;
    }

    /**
     * 只用了两字节
     *
     * @param len
     * @return
     */
    public Packet write(int len) {
        bs[this.len++] = (byte) (len & 0XFF);
        bs[this.len++] = (byte) ((len >> 8) & 0XFF);
        return this;
    }

    public DatagramPacket getDatagramPacketForWrite() {
        return new DatagramPacket(bs, bs.length);//创建Packet相当于创建集装箱
    }

    public DatagramPacket getDatagramPacketForRead(InetAddress address, int port) {
        return new DatagramPacket(bs, len, address, port);//创建Packet相当于创建集装箱
    }

    @Override
    public String toString() {
        int who = 8000 + bs[0];

        String type = "start";
        switch (bs[1]) {
            case TYPE_START:
                type = "start";
                break;
            case TYPE_TRACE_ID:
                type = "traceId";
                break;
            case TYPE_MULTI_TRACE_ID:
                type = "multi_trace_id";
                break;
            case TYPE_LOG:
                type = "log";
                break;
            case TYPE_MULTI_LOG:
                type = "multi_log";
                break;
            case TYPE_END:
                type = "end";
                break;
        }
        if (bs[1] == TYPE_MULTI_LOG) {
            StringBuilder sb = new StringBuilder("who:" + who + ", type:" + type + ", len=" + (len - 2) + ", data=\n");
            for (int i = 2; i < this.len; ) {
                int l = (bs[i] & 0XFF) + ((bs[i + 1] & 0XFF) << 8);
                sb.append(l + " " + new String(bs, i + 2, l));
                i = i + 2 + l;
            }
            return sb.toString();
        } else {
            return "who:" + who + ", type:" + type + ", len=" + (len - 2) + ", data=\n" + new String(bs, 2, len - 2);
        }
    }
}
