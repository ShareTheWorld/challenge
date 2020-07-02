package com.aliyun.filter;


import com.aliyun.common.Packet;

import java.util.HashSet;
import java.util.Set;

public class Page {
    private static final int SKIP_LEN = 110;//跳过长度

    public static final int LEN = 32 * 1024 * 1024;//存放数据的缓冲区，太大了会导致缓存页不停的失效
    public int pageIndex = 0;//表示这是第几页
    public byte[] data = new byte[32 * 1024 * 1024 + 1024];//存放数据的缓冲区，太大了会导致缓存页不停的失效
    public int len = 0;//data中存储数据的长度
    public int bucket[][][] = new int[0X10000][][];//64K 6.5万条  256K
    //每页：4000>不同的traceId，100>重复的traceId的最大数，2表示开始位置和长度  a=4000,b=100,c=2
    public int link[][][] = new int[10000][2][200];//data[i][0][0]存的hash;  data[i][0][0]存的高度, 4.6M
    public int p;//表示当前link取到第几个位置了

    public byte[] err = new byte[2048];//可以存放128个错误了
    public int errLen = 0;

    private boolean isHandle = false;

    public int testLineNumber = 0;
    public static Set<String> testCountErrorSet = new HashSet<>();
    public static Set<Integer> countHashSet = new HashSet<>();
    public static int logMinLength = 2000;//日志最小长度

    public Packet errPkt;//用于存放错误,可以放64个错误

    //下面是建立索引的字段
    public void createIndexAndFindError() {
        long start_time = System.currentTimeMillis();
        int i = 0;
        do {
            int hash = hash(data, i);
            //获取一行数据
            int l = getLine(data, i);
            put(hash, i, l);
            i = i + l;
        } while (i != len);//如果恰好等于的话，就说明刚好到达最后了,这样getLog就不需要进行边界判断了
//        System.out.println("create index and find error,page=" + pageIndex + ", time=" + (System.currentTimeMillis() - start_time));
//        System.out.println("pageIndex:" + pageIndex + ",totalLineCount:" + testLineNumber + ",distinctLineCount:" + countErrorSet.size() + ",hashCount:" + countHashSet.size());
    }

    public int hash(byte[] d, int i) {
        return (d[i] + (d[i + 1] << 3) + (d[i + 2] << 6) + (d[i + 3] << 9) + (d[i + 4] << 12)) & 0XFFFF;
    }


    public int getLine(byte[] d, int s) {
        int i = s + SKIP_LEN;
        //开始寻早error=1和!http.status_code=200 和\n
        while (d[i++] != '\n') {
            if (d[i] == '=') {
                //TODO 可以更具字符出现频率，做逻辑上的先后顺序  u2.58 p 2.89 d 3.91
                if (
//                        d[i - 16] == 'h' && d[i - 15] == 't' && d[i - 14] == 't' && d[i - 13] == 'p'
//                        && d[i - 12] == '.' && d[i - 11] == 's' && d[i - 10] == 't' && d[i - 9] == 'a'
//                        && d[i - 8] == 't' && d[i - 7] == 'u' && d[i - 6] == 's' &&
                        d[i - 5] == '_'
//                        && d[i - 4] == 'c' && d[i - 3] == 'o' && d[i - 2] == 'd' && d[i - 1] == 'e'
                                && (d[i + 1] != '2' || d[i + 2] != '0' || d[i + 3] != '0')) {
                    System.arraycopy(d, s, err, errLen, 16);
                    errLen += 16;
                } else if (
                        (d[i - 6] == '&' || d[i - 6] == '|') &&
//                        d[i - 5] == 'e' &&
//                                d[i - 4] == 'r' &&
//                                d[i - 3] == 'r' &&
//                                d[i - 2] == 'o' &&
//                                d[i - 1] == 'r' &&
                                d[i + 1] == '1') {
//                    System.out.println(new String(d, i - 20, 50));
                    System.arraycopy(d, s, err, errLen, 16);
                    errLen += 16;
                }
            }
        }
        return i - s;
    }


    public void put(int hash, int s, int len) {
        int[][] tmp;
        tmp = bucket[hash];
        if (tmp == null) {
            tmp = bucket[hash] = link[p++];
            tmp[0][0] = hash;
            tmp[1][0] = 1;
        }
        try {
            //tmp[1][0]存储的是有多长
            tmp[0][tmp[1][0]] = s;
            tmp[1][tmp[1][0]] = len;
            tmp[1][0]++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int selectByTraceId(byte k[], Log[] logs, int start) {
        int hash = hash(k, 0);//k[0] + (k[1] << 3) + (k[2] << 6) + (k[3] << 9) + (k[4] << 12)) & 0XFFFF;
        int link[][] = bucket[hash];
        if (link == null) return 0;
        int p = start;
        int count = link[1][0];
        for (int i = 1; i < count; i++) {
            //start=link[0][i] len=link[1][i]
            boolean b = startsWith(data, link[0][i], k);//会增加耗时  需要engine去做过滤
            if (b) {
                logs[p++] = new Log(this.data, link[0][i], link[1][i]);
            }
        }
        return p - start;
    }

    /**
     * 从data的s位置开始，判断data是否包含key
     */
    private static boolean startsWith(byte data[], int s, byte key[]) {
        for (int i = 0; i < key.length; i++) {
            if (data[s + i] != key[i]) return false;
        }
        return true;
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


    public void clear() {
        //清除数据
        len = 0;

        isHandle = false;

        //清除索引
        for (int i = 0; i < p; i++) {
//            data[i][0][0]存的hash;  data[i][0][0]存的高度
            bucket[link[i][0][0]] = null;//将这个位置质为null
//            data[i][0][0] = 0;//将高度置0，可以不要这个指令，后面在使用的时候会设置
        }
        p = 0;

        //清除errorPacket中的错误
        errLen = 0;
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
