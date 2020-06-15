package com.aliyun.filter;


import com.aliyun.common.Packet;

import java.util.*;

class Page {
    private static final int SKIP_LEN = 100;//跳过长度
    public static int min = 64 * 1024 * 1024;//要求读数据的最小长度

    public int pageIndex;
    public byte[] data = new byte[64 * 1024 * 1024];//用于存放数据,+100是避免数据访问越界 32M可存22万条，每条（150）
    public int len;//用于存放数据的长度

    public static final int bucketLen = 0X100000;
    public List<Log>[] bucket = new List[bucketLen];//每一个traceId大概会有20条
    public Packet errorPacket;
    private boolean isHandle = false;

    private static int count[] = new int[256];
    private static int testErrorCount = 0;
    public static int testLineNumber = 0;
    public static Set<String> countErrorSet = new HashSet<>();
    public static Set<Integer> countHashSet = new HashSet<>();
    public static int logMinLength = 2000;//日志最小长度

    //下面是建立索引的字段
    public void createIndex() {
        if (isHandle) return;
        isHandle = true;
        errorPacket = Data.errorPackets[pageIndex / Data.PER_HANDLE_PAGE_NUM];
        int i = 0;
        do {
            int index = hash(data, i);
            try {
                if (bucket[index] == null) bucket[index] = new ArrayList(32);//平均大小17.5
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log log = getLog(data, i, len);
            countErrorSet.add(new String(data, log.start, 16));
            bucket[index].add(log);
            i += log.len;
            testLineNumber++;
        } while (i != len);//如果恰好等于的话，就说明刚好到达最后了,这样getLog就不需要进行边界判断了
        System.out.println("页码：" + pageIndex + ",总行数:" + testLineNumber + ",不重复行数:" + countErrorSet.size() + ",哈希数:" + countHashSet.size());
    }


    private int hash(byte data[], int s) {
//        int index1 = (data[s] << 12) + (data[++s] << 8) + (data[++s] << 4) + (data[++s]);// + (data[++s] << 16) + (data[++s] << 20));
//        int index2 = (data[++s] << 12) + (data[++s] << 8) + (data[++s] << 4) + (data[++s]);// + (data[++s] << 16) + (data[++s] << 20));
//        int index = (index1 ^ index2) & 0xFFFF;
//        countHashSet.add(index);
//        return index;
        int index1 = (data[++s] << 15) + (data[++s] << 12) + (data[++s] << 9) + (data[++s] << 6) + (data[++s] << 3) + data[++s];
//        int index1 = (data[++s] << 15) + (data[++s] << 10) + (data[++s] << 5) + data[++s];// + (data[++s] << 16) + (data[++s] << 20));
//        int index2 = (data[++s] << 15) + (data[++s] << 10) + (data[++s] << 5) + (data[++s]);// + (data[++s] << 16) + (data[++s] << 20));
//        int index = (index1) & 0xFFFFF;
//        countHashSet.add(index);
//        return index;
        return (index1) & 0xFFFFF;
    }


    public Log getLog(byte[] data, int s, int len) {
        int i = s + SKIP_LEN - 1;
        //开始寻早error=1和!http.status_code=200 和\n
        boolean isError = false;
        while (true) {
            /* ==========、判断一 跳过指定位置开始寻找错误和换行符===========*/
            /*
            //可以判断是否小于'='在进去，如果有分支预测技术的话，会增加新能，=和\n成功的次数是20%
            if (data[++i] == '=') {
//                count[data[i + 2]]++;
                //TODO 可以更具字符出现频率，做逻辑上的先后顺序  u2.58 p 2.89 d 3.91
                if (data[i - 5] == '_' && (data[i + 1] != '2' || data[i + 2] != '0' || data[i + 3] != '0')
                        && data[i - 16] == 'h' && data[i - 15] == 't' && data[i - 14] == 't' && data[i - 13] == 'p'
                        && data[i - 12] == '.' && data[i - 11] == 's' && data[i - 10] == 't' && data[i - 9] == 'a'
                        && data[i - 8] == 't' && data[i - 7] == 'u' && data[i - 6] == 's'
                        && data[i - 4] == 'c' && data[i - 3] == 'o' && data[i - 2] == 'd' && data[i - 1] == 'e') {
                    isError = true;
                } else if (data[i + 1] == '1' && data[i - 5] == 'e' && data[i - 4] == 'r' && data[i - 3] == 'r' && data[i - 2] == 'o'
                        && data[i - 1] == 'r') {
                    isError = true;
                }
            } else if (data[i] == '\n') {
                break;
            }
           */

            /* ==========、判断二 假设错误出现在最后一个位置===========*/
            if (data[++i] == '\n') {
                if (data[i - 9] == '_' &&
                        data[i - 20] == 'h' && data[i - 19] == 't' && data[i - 18] == 't' && data[i - 17] == 'p' &&
                        data[i - 16] == '.' && data[i - 15] == 's' && data[i - 14] == 't' && data[i - 13] == 'a' &&
                        data[i - 12] == 't' && data[i - 11] == 'u' && data[i - 10] == 's' &&
                        data[i - 8] == 'c' && data[i - 7] == 'o' && data[i - 6] == 'd' && data[i - 5] == 'e' &&
                        data[i - 4] == '=' && (data[i - 3] != '2' || data[i - 2] != '0' || data[i - 1] != '0')) {
                    isError = true;
                } else if (data[i - 7] == 'e' && data[i - 6] == 'r' &&
                        data[i - 5] == 'r' && data[i - 4] == 'o' &&
                        data[i - 3] == 'r' && data[i - 2] == '=' && data[i - 1] == '1') {
                    isError = true;
                }
                break;
            }
        }
        if (i - s + 1 < logMinLength) logMinLength = i - s + 1;
        Log log = new Log(s, i - s + 1, isError);
        if (isError) errorPacket.write(data, s, 16);
        return log;
    }

    public List<byte[]> selectByTraceId(byte traceId[]) {
        int index = hash(traceId, 0);
        List<Log> list = bucket[index];
        if (list == null) return new ArrayList<>();
        List<byte[]> results = new ArrayList<>(list.size());

        for (int i = 0; i < list.size(); i++) {
            Log log = list.get(i);
            boolean bool = startsWith(this.data, log.start, traceId);
            if (!bool) continue;
            byte[] bs = new byte[log.len];
            System.arraycopy(this.data, log.start, bs, 0, log.len);
            results.add(bs);
        }
        return results;
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

    public void clear() {
        len = 0;
        isHandle = false;
        bucket = new List[bucketLen];
    }

}
