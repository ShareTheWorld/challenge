package com.aliyun.filter.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Page {
    public byte[] data = new byte[32 * 1024 * 1024];//用于存放数据,+100是避免数据访问越界
    public static int min = 32 * 1024 * 1024 - 4028;//要求读数据的最小长度
    public int len;//用于存放数据的长度
    public int indexLen = 3;//索引使用字符的长度
    public List<Log>[] bucket = new List[0X10000];
    private static final int SKIP_LEN = 235;//跳过长度


    private static int count[] = new int[256];
    private static int testErrorCount = 0;
    private static int testTotalCount = 0;
    public static Set<String> countErrorSet = new HashSet<>();

    //下面是建立索引的字段
    public void createIndex() {
        int i = 0;
        do {
            int index = getIndex(data, i);
            if (bucket[index] == null) bucket[index] = new ArrayList(32);//平均大小17.5
            Log log = get(data, i, len);
            bucket[index].add(log);
            i += log.len;
        } while (i < len);
        testTotalCount += len;
//        System.out.println("testErrorCount=" + testErrorCount);
//        System.out.println("countErrorSet=" + countErrorSet.size());
//        System.out.println("testTotalCount=" + testTotalCount);
//        if (len < 10 * 1024 * 1024) {
//            for (int j = 0; j < count.length; j++) {
//                System.out.println(((char) j) + "=" + count[j]);
//            }
//        }
    }


    private static int getIndex(byte data[], int j) {
        int index1 = (data[j] << 12) + (data[++j] << 8) + (data[++j] << 4) + (data[++j]);
        int index2 = (data[++j] << 12) + (data[++j] << 8) + (data[++j] << 4) + (data[++j]);
        return (index1 ^ index2) & 0xFFFF;
    }


    public static Log get(byte[] data, int s, int len) {
        int i = s + SKIP_LEN - 1;
        //开始寻早error=1和!http.status_code=200 和\n
        boolean isError = false;
        while (i < len) {
          /* ======================================判断一=============================================
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
            if (data[++i] == '\n') {
                if (data[i - 9] == '_' &&
//                        data[i - 20] == 'h' && data[i - 19] == 't' && data[i - 18] == 't' && data[i - 17] == 'p' &&
//                        data[i - 16] == '.' && data[i - 15] == 's' && data[i - 14] == 't' && data[i - 13] == 'a' &&
//                        data[i - 12] == 't' && data[i - 11] == 'u' && data[i - 10] == 's' &&
//                        data[i - 8] == 'c' && data[i - 7] == 'o' && data[i - 6] == 'd' && data[i - 5] == 'e' &&
                        data[i - 4] == '=' && (data[i - 3] != '2' || data[i - 2] != '0' || data[i - 1] != '0')
                ) {
                    isError = true;
                } else if (
//                        data[i - 7] == 'e' && data[i - 6] == 'r' && data[i - 5] == 'r'&&
//                                 data[i - 4] == 'o'&&
                                data[i - 3] == 'r' && data[i - 2] == '=' && data[i - 1] == '1') {
                    isError = true;
                }
                break;
            }
        }
        if (isError) {
            ++testErrorCount;
            countErrorSet.add(new String(data, s, 15));
//            System.out.println(testErrorCount + "  " + new String(data, s, i - s + 1));
        }
        return new Log(isError, s, i - s + 1);
    }

    public List selectByTraceId() {
        return null;
    }
}

class Log {
    boolean isError;
    int start;
    int len;

    public Log(boolean isError, int start, int len) {
        this.isError = isError;
        this.start = start;
        this.len = len;
    }
}