package com.aliyun.filter.service;

import java.util.ArrayList;
import java.util.List;

class Page {
    public byte[] data = new byte[32 * 1024 * 1024];//用于存放数据
    public static int min = 32 * 1024 * 1024 - 4028;//要求读数据的最小长度
    public int len;//用于存放数据的长度
    public int indexLen = 3;//索引使用字符的长度
    public List<Log>[] bucket = new List[64 * 64 * 64];
    private static final int SKIP_LEN = 190;//跳过长度

    static long testTotal = 0;
    static long testErrorTotal = 0;
    static long testErrorTotal1 = 0;
    static long testErrorTotal2 = 0;

    //下面是建立索引的字段
    public void createIndex() {
        int i = 0;
        do {
//            int index = get(data[i]) * get(data[i + 1]) * get(data[i + 2]);
            int index = (get(data[i]) << 12) + (get(data[i + 1]) << 6) + get(data[i + 2]);
            if (bucket[index] == null) bucket[index] = new ArrayList(20);
            Log log = get(data, i);
            bucket[index].add(log);
            testTotal += log.len;
//            System.out.println(testTotal);
            i += log.len;
        } while (i < len);
//        System.out.println("testErrorTotal=" + testErrorTotal);
//        System.out.println("testErrorTotal1=" + testErrorTotal1);
//        System.out.println("testErrorTotal2=" + testErrorTotal2);
    }

    private int get(byte b) {
        if (b >= 97) return b - 61;//b-97+36
        if (b >= 65) return b - 55;//b-65+10
        return b - 48;
    }

    public Log get(byte[] data, int s) {
        int i = s + SKIP_LEN;
        //开始寻早error=1和!http.status_code=200 和\n
        boolean isError = false;
        int b;
        while (i < len) {
            b = data[i];
//            if (b <= 61) {
            if (b == '=') {
                //TODO 可以更具字符出现频率，做逻辑上的先后顺序  u2.58 p 2.89 d 3.91
                if (data[i - 5] == '_' && (data[i + 1] != '2' || data[i + 2] != '0' || data[i + 3] != '0')
                        && data[i - 16] == 'h' && data[i - 15] == 't' && data[i - 14] == 't' && data[i - 13] == 'p'
                        && data[i - 12] == '.' && data[i - 11] == 's' && data[i - 10] == 't' && data[i - 9] == 'a'
                        && data[i - 8] == 't' && data[i - 7] == 'u' && data[i - 6] == 's'
                        && data[i - 4] == 'c' && data[i - 3] == 'o' && data[i - 2] == 'd' && data[i - 1] == 'e') {
                    isError = true;
//                    testErrorTotal2++;
//                    System.out.print("http.status_code=200  ");
                } else if (data[i + 1] == '1' && data[i - 5] == 'e' && data[i - 4] == 'r' && data[i - 3] == 'r' && data[i - 2] == 'o'
                        && data[i - 1] == 'r') {
                    isError = true;
//                    testErrorTotal1++;
//                    System.out.print("error=1   ");
                }
            } else if (b == '\n') {
                break;
            }
//            }
            i++;
        }
        if (isError) {
            ++testErrorTotal;
//            System.out.println(++testErrorTotal + "  " + new String(data, s, i - s + 1));
        }


        return new Log(isError, s, i - s + 1);
    }

    public List selectByTraceId() {
        return null;
    }
}

class Log {
    boolean isError = false;
    int start;
    int len;

    public Log(boolean isError, int start, int len) {
        this.isError = isError;
        this.start = start;
        this.len = len;
    }
}