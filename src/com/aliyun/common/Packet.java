package com.aliyun.common;

public class Packet {
    public static final byte TYPE_START = 1;
    public static final byte TYPE_TRACE_ID = 2;//traceId
    public static final byte TYPE_MULTI_TRACE_ID = 3;//pageIndex traceId traceId traceId
    public static final byte TYPE_LOG = 4;//len log
    public static final byte TYPE_MULTI_LOG = 5;//len log len log len log
    public static final byte TYPE_END = 0;
    public static final byte TRACE_ID_LEN = 16;

    public static final int P_LEN = 0;
    public static final int P_WHO = 3;
    public static final int P_TYPE = 4;
    public static final int P_PAGE = 5;
    public static final int P_DATA = 7;
    private byte bs[];//[len0,len1,len2,who,type,data] data=len data len data
    private int len = 7;//代表bs的使用长度
    public boolean isHandle;//是否处理，主要是在计算校验和的时候需要用一下，不符合设计规范，主要是为了提高速度

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

    public void clear() {
        int len = 0;
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

    public int getPage() {
        int page = ((bs[P_PAGE + 0] & 0XFF) << 8) + (bs[P_PAGE + 1] & 0XFF);
        return page;
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

    public void reset(byte who, byte type) {
        bs[P_LEN + 0] = 0;
        bs[P_LEN + 1] = 0;
        bs[P_LEN + 2] = 0;
        this.bs[P_WHO] = who;
        this.bs[P_TYPE] = type;
        len = 7;
    }


    /**
     * 只用了两字节
     *
     * @param len
     * @return
     */
    public Packet writeWithDataLen(byte bs[], int start, int len) {
        this.bs[this.len++] = (byte) ((len >> 8) & 0XFF);
        this.bs[this.len++] = (byte) (len & 0XFF);
        System.arraycopy(bs, start, this.bs, this.len, len);
        this.len += len;
        return this;
    }

    /**
     * 只用了两字节
     */
    public Packet writePage(int page) {
        this.bs[P_PAGE + 0] = (byte) ((page >> 8) & 0XFF);
        this.bs[P_PAGE + 1] = (byte) (page & 0XFF);
        return this;
    }


    @Override
    public int hashCode() {
//        if (len == 7) return 0;
//        //对于TYPE_MULTI_LOG 类型的数据是从这个位置开始存放traceId的
//        int index1 = (bs[5] << 12) + (bs[6] << 8) + (bs[7] << 4) + (bs[8]);// + (data[++s] << 16) + (data[++s] << 20));
//        int index2 = (bs[9] << 12) + (bs[10] << 8) + (bs[11] << 4) + (bs[12]);// + (data[++s] << 16) + (data[++s] << 20));
//        return (index1 ^ index2) & 0xFFFF;
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        //对于TYPE_MULTI_LOG 类型的数据是从这个位置开始存放traceId的
        Packet p = (Packet) obj;
        for (int s = P_DATA; s < P_DATA + 16; s++) {//这个范围存放的是traceId
            if (this.bs[s] != p.bs[s]) return false;
        }
        return true;
//        return super.equals(obj);
    }

    @Override
    public String toString() {
        int who = 8000 + bs[P_WHO];
        int page = getPage();

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

            StringBuilder sb = new StringBuilder("total len:" + len +
                    ", who:" + who +
                    ", type:" + type +
                    ", page:" + page +
                    ", data len=" + (len - P_DATA) +
                    ", traceId=" + new String(bs, P_DATA, TRACE_ID_LEN) +
                    ", data=\n");
            for (int i = P_DATA + TRACE_ID_LEN; i < this.len; ) {
                int l = ((bs[i] & 0XFF) << 8) + (bs[i + 1] & 0XFF);
//                System.out.println(i + "   " + l + "  " + len);
                sb.append(l + " " + new String(bs, i + 2, l));//2表示使用了两个字节表示长度
                i = i + 2 + l;
            }
            return sb.toString();
        } else {
            return "total len:" + len +
                    ", who:" + who +
                    ", type:" + type +
                    ", page:" + page +
                    ", len=" + (len - P_DATA) +
                    ", data=\n" + new String(bs, P_DATA, len - P_DATA);
        }
    }
}

