package com.aliyun.filter;

import com.aliyun.common.Packet;

import static com.aliyun.common.Const.filter;
import static com.aliyun.common.Const.who;

public class Buffer {
    public static final int LEN = 8 * 1024 * 1024;//存放数据的缓冲区，太大了会导致缓存页不停的失效
    public int page = 0;//表示这是第几页
    public byte[] data = new byte[8 * 1024 * 1024 + 1024];//存放数据的缓冲区，太大了会导致缓存页不停的失效
    public int len = 0;//data中存储数据的长度
    public int bucket[][][] = new int[0X10000][][];//64K 6.5万条  256K
    //每页：4000>不同的traceId，100>重复的traceId的最大数，2表示开始位置和长度  a=4000,b=100,c=2
    public int link[][][] = new int[3000][2][200];//data[i][0][0]存的hash;  data[i][0][0]存的高度, 4.6M
    public int p;//表示当前link取到第几个位置了
    //0表示是空的，可以再次使用，1表示建立完索引
    public int status;
    public Packet errPkt = new Packet(1, who, Packet.TYPE_MULTI_TRACE_ID);//用于存放错误,可以放64个错误


    public void setPage(int page) {
        this.page = page;
        errPkt.writePage(page);
    }

    public void put(int hash, int s, int len) {
        int[][] tmp;
        tmp = bucket[hash];
        if (tmp == null) {
            tmp = bucket[hash] = link[p++];
            tmp[0][0] = hash;
            tmp[1][0] = 1;
        }
        //tmp[1][0]存储的是有多长
        tmp[0][tmp[1][0]] = s;
        tmp[1][tmp[1][0]] = len;
        tmp[1][0]++;
    }

    public void clear() {
        //清除数据
        len = 0;

        //清除索引
        for (int i = 0; i < p; i++) {
//            data[i][0][0]存的hash;  data[i][0][0]存的高度
            bucket[link[i][0][0]] = null;//将这个位置质为null
//            data[i][0][0] = 0;//将高度置0，可以不要这个指令，后面在使用的时候会设置
        }
        p = 0;

        //清除错误
        errPkt.reset(who, Packet.TYPE_MULTI_TRACE_ID);
    }


    public void select(Packet packet) {
        byte[] bs = packet.getBs();
        int len = packet.getLen();
        for (int i = Packet.P_DATA; i < len; i += 16) {
            select(bs, i);
        }
    }

    Packet pkt = new Packet(64, who, Packet.TYPE_MULTI_LOG);

    public void select(byte bs[], int s) {
        pkt.reset(who, Packet.TYPE_MULTI_LOG);
        pkt.writePage(page);
        pkt.write(bs, s, 16);//需要先写入一个traceId
        //找到hash所在位置
        int hash = (bs[s] + (bs[s + 1] << 3) + (bs[s + 2] << 6) + (bs[s + 3] << 9) + (bs[s + 4] << 12)) & 0XFFFF;
        int link[][] = bucket[hash];
        if (link != null) {
            int count = link[1][0];
            for (int i = 1; i <= count; i++) {
                //start=link[0][i] len=link[1][i]
//                boolean b = equals(data, link[0][i], bs, s);//会增加耗时  需要engine去做过滤
//                if (b) {
                pkt.writeWithDataLen(data, link[0][i], link[1][i]);
//                }
            }
        }

        //TODO 发送packet
        filter.sendPacket(pkt);
//        System.out.println(pkt);

    }

    /**
     * 比较两个byte是否相等
     */
    public static boolean equals(byte data[], int ds, byte key[], int ks) {
        for (int i = 0; i < 16; i++) {
            if (data[ds + i] != key[ks + i]) return false;
        }
        return true;
    }


    public static void main(String args[]) {
        Buffer map = new Buffer();
        map.put(0, 5, 100);
        map.put(0, 5, 100);
        map.put(1, 5, 100);
        map.put(2, 5, 100);
        map.put(3, 5, 100);
        System.out.println(map);
//        map.put(0, 5, 100);
//        map.put(0, 5, 100);
//        map.put(0, 5, 100);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("total=" + p + "\n");
        for (int i = 0; i < bucket.length; i++) {
            if (bucket[i] == null) continue;
            sb.append(i + " : \n");
            for (int j = 0; j < bucket[i].length; j++) {
                for (int k = 0; k < bucket[i][1][0]; k++) {
                    sb.append(getN(bucket[i][j][k]));
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        sb.append("\n\n\n\n");
        return sb.toString();
    }

    private String getN(int n) {
        String str = n + "          ";
        return str.substring(0, 10);
    }
}
