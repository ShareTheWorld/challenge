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

    public static final int P_LEN = 0;
    public static final int P_WHO = 3;
    public static final int P_TYPE = 4;
    public static final int P_DATA = 5;
    private byte bs[];//[len0,len1,len2,who,type,data] data=len data len data
    private int len = 5;//代表bs的使用长度

    public Packet(int k) {
        bs = new byte[k * 1024];
    }

    public Packet(byte bs[], int len) {
        this.bs = bs;
        this.len = len;
    }


    public Packet(int k, byte who, byte type) {
        bs = new byte[k * 1024];
        this.bs[P_WHO] = who;
        this.bs[P_TYPE] = type;
    }

    public int getLen() {
        if (len <= 0) {
            len = ((bs[P_LEN] & 0XFF) << 16) + ((bs[P_LEN + 1] & 0XFF) << 8) + (bs[P_LEN + 2] & 0XFF);
        }
        return len;
    }

    public int getWho() {
        return bs[P_WHO];
    }

    public int getType() {
        return bs[P_TYPE];
    }

    public byte[] getBs() {
        //将长度写入到对应的位置
        bs[P_LEN + 0] = (byte) ((len >> 16) & 0XFF);
        bs[P_LEN + 1] = (byte) ((len >> 8) & 0XFF);
        bs[P_LEN + 2] = (byte) (len & 0XFF);
        return bs;
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
        bs[this.len++] = (byte) ((len >> 8) & 0XFF);
        bs[this.len++] = (byte) (len & 0XFF);
        return this;
    }


    @Override
    public String toString() {
        int who = 8000 + bs[P_WHO];

        String type = "start";
        switch (bs[P_TYPE]) {
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
        if (bs[P_TYPE] == TYPE_MULTI_LOG) {

            StringBuilder sb = new StringBuilder("total len:" + len + ", who:" + who + ", type:" + type + ", data len=" + (len - P_DATA) + ", data=\n");
            for (int i = P_DATA; i < this.len; ) {
                int l = ((bs[i] & 0XFF) << 8) + (bs[i + 1] & 0XFF);
//                System.out.println(i + "   " + l + "  " + len);
                sb.append(l + " " + new String(bs, i + 2, l));//2表示使用了两个字节表示长度
                i = i + 2 + l;
            }
            return sb.toString();
        } else {
            return "total len:" + len + ", who:" + who + ", type:" + type + ", len=" + (len - P_DATA) + ", data=\n" + new String(bs, P_DATA, len - P_DATA);
        }
    }
}
