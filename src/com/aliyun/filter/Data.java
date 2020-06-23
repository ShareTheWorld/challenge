package com.aliyun.filter;

import com.aliyun.common.Const;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.aliyun.common.Const.data_port;

public class Data {
    //8M 4万条，去掉重复过后是2100条无重复的traceId
//    private static byte[] buf = new byte[8 * 1024 * 1024];//存放数据的缓冲区，太大了会导致缓存页不停的失效
    private static byte[] tail = new byte[1024];//尾巴数据
    private static int tailLen = 0;//尾巴数据的长度

    private static int SKIP_LEN = 130;//每一行跳过长度 4G文件是trace1.data是133，trace2.data是131

    //    private static int bucket[] = new int[0X10000];
    private static Buffer buf = new Buffer();


    private static int testTotalCount = 0;//总行数
    public static Set<String> testErrorTraceIdSet = new HashSet<>();//错误traceId
    public static Set<String> testTraceIdSet = new HashSet<>(10000);//所有traceId
    public static Set<Integer> testHashSet = new HashSet<>(10000);//哈希数
    public static int testMinLineLen = 1000;//行的最小长度
    public static Set<String> params = new HashSet<>();

    public static void start() throws Exception {
        long start_time = System.currentTimeMillis();
//        InputStream in = new FileInputStream("/Users/fht/d_disk/chellenger/data3/trace1.data");

        String path = "http://127.0.0.1:" + data_port + "/trace" + (Const.who + 1) + ".data ";
        System.out.println(path);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        InputStream in = conn.getInputStream();
        int n, len;
        byte[] data;
        int pageIndex = 0;
        do {
            pageIndex++;
            data = buf.data;
            //将尾巴复制到缓冲区中
            System.arraycopy(tail, 0, data, 0, tailLen);
            len = tailLen;

            //读取一页数据，
            while ((n = in.read(data, len, data.length - len)) != -1) {
                len += n;
                if (len == data.length) break;
            }


            //反向找到换行符
            for (tailLen = 0; tailLen < 1000; tailLen++) {
                if (data[len - 1 - tailLen] == '\n') {//
                    System.arraycopy(data, len - tailLen, tail, 0, tailLen);
                    break;
                }
            }
            buf.clear();
            buf.len = len - tailLen;
            handleData(data, len - tailLen);

//            System.out.println("traceId count:" + testTraceIdSet.size());
//            System.out.println(" hash count:" + testHashSet.size());
//            System.out.println(" error traceId count:" + testErrorTraceIdSet.size());
//            testHashSet.clear();
//            testTraceIdSet.clear();
//            testErrorTraceIdSet.clear();
        } while (n != -1);
//        System.out.println(map);
        System.out.println(" error traceId count:" + testErrorTraceIdSet.size());
//        System.out.println("mine line len is :" + testMinLineLen);
        System.out.println("time=" + (System.currentTimeMillis() - start_time));
        System.out.println("total count=" + testTotalCount);
        System.out.println("params size=" + params.size());
        System.out.println(params);
    }

    private static void handleData(byte[] data, int len) {
        int i = 0;
        do {
//            testTraceIdSet.add(new String(buf, i, 16));
            int s = i;

            //计算索引
            int hash = (data[i] + (data[i + 1] << 3) + (data[i + 2] << 6) + (data[i + 3] << 9) + (data[i + 4] << 12)) & 0XFFFF;
//            testHashSet.add(index);
//            bucket[index] = 1;
            //获取一行数据
            i += SKIP_LEN;
            while (data[i++] != '\n') ;//找到了换行符
//            if (testMinLineLen > i - s) testMinLineLen = i - s;//统计最小长度
            //判断是否有错
            if (data[i - 22] == '&' &&
//                    buf[i - 21] == 'h' && buf[i - 20] == 't' && buf[i - 19] == 't' && buf[i - 18] == 'p' &&
//                    buf[i - 17] == '.' && buf[i - 16] == 's' && buf[i - 15] == 't' && buf[i - 14] == 'a' &&
//                    buf[i - 13] == 't' && buf[i - 12] == 'u' && buf[i - 11] == 's' &&
//                    buf[i - 10] == '_' &&
//                    buf[i - 9] == 'c' && buf[i - 8] == 'o' && buf[i - 7] == 'd' && buf[i - 6] == 'e' &&
//                    buf[i - 5] == '=' &&
//                    (buf[i - 4] != '2' || buf[i - 3] != '0' || buf[i - 2] != '0') &&
                    data[i - 4] != 2) {
//                testErrorTraceIdSet.add(new String(buf, s, 16));
//                System.out.print(testErrorTraceIdSet.size() + "\t" + new String(buf, i - 25, 25));
            } else if (data[i - 9] == '&' &&
//                    buf[i - 8] == 'e' && buf[i - 7] == 'r' &&
//                    buf[i - 6] == 'r' && buf[i - 5] == 'o' &&
//                    buf[i - 4] == 'r' && buf[i - 3] == '=' &&
                    data[i - 2] == '1') {
//                testErrorTraceIdSet.add(new String(buf, s, 16));
//                System.out.print(testErrorTraceIdSet.size() + "\t" + new String(buf, i - 25, 25));
            }
            buf.put(hash, s, i - s);
            testTotalCount++;
        } while (i != len);
    }
}
