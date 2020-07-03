package com.aliyun.test;

import java.io.FileInputStream;

public class Test {
    static byte[] buf = new byte[1 * 1024 * 1024 * 1024];
    static int len = 0;
    static int errorCount = 0;

    public static void main(String args[]) throws Exception {
        FileInputStream in = new FileInputStream("/Users/fht/d_disk/chellenger/data/trace1.data");
        int l;
        //读取一页数据，
        while ((l = in.read(buf, len, buf.length)) != -1) {
            len += l;
            if (len == buf.length) break;
        }

        //反向找到换行符
        int tailLen = 0;
        for (tailLen = 0; tailLen < 1024; tailLen++) {
            if (buf[len - 1 - tailLen] == '\n') {//
                break;
            }
        }

        len -= tailLen;

        long startTime = System.currentTimeMillis();
        int i = 0;
        do {
            int n = getLine1(buf, i);
            i += n;
        } while (i != len);
        System.out.println("time=" + (System.currentTimeMillis() - startTime) + ", errorCount=" + errorCount);


//        startTime = System.currentTimeMillis();
//        i = 0;
//        do {
//            int n = getLine1(buf, i);
//            i += n;
//        } while (i != len);
//        System.out.println("time=" + (System.currentTimeMillis() - startTime) + ", errorCount=" + errorCount);


    }

    public static int getLine(byte[] d, int s) {
        int i = s + 100;
        while (true) {
            do {
                if (d[i++] == 10) {
                    return i - s;
                }
            } while (d[i] != 61);
            if (d[i - 5] == 95 && (d[i + 1] != 50 || d[i + 2] != 48 || d[i + 3] != 48)) {
                ++errorCount;
            } else if ((d[i - 6] == 38 || d[i - 6] == 124) && d[i + 1] == 49) {
                ++errorCount;
            }
        }
    }

    public static int getLine1(byte[] d, int s) {
        int i = s + 110;
        //开始寻早error=1和!http.status_code=200 和\n

        while (d[++i] != '\n') {
            if (d[i] == '=') {
                //TODO 可以更具字符出现频率，做逻辑上的先后顺序  u2.58 p 2.89 d 3.91
                if (d[i - 5] == '_') {
                    if (d[i + 1] != '2') {
                        errorCount++;
                    }
                    break;
                } else if ((d[i - 6] == '&' || d[i - 6] == '|')) {
                    errorCount++;
                    break;
                }
            }
        }
        if (d[i] != '\n') {
            while (d[++i] != '\n') ;
            return i - s + 1;
        }

        return i - s + 1;
    }
}


//for (; ; i++) {
//        if (d[i] == '=') {
//        //TODO 可以更具字符出现频率，做逻辑上的先后顺序  u2.58 p 2.89 d 3.91
//        if (d[i - 5] == '_' && (d[i + 1] != '2' || d[i + 2] != '0' || d[i + 3] != '0')) {
//        errorCount++;
//        } else if ((d[i - 6] == '&' || d[i - 6] == '|') && d[i + 1] == '1') {
//        errorCount++;
//        }
//        } else if (d[i] == '\n') {
//        i++;
//        break;
//        }
//        }