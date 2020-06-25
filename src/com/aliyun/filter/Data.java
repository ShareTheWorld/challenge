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

    private static int FIND_CR_SKIP_LEN = 100;//发现换行符跳过的长度 4G文件是trace1.data是133，trace2.data是131
    private static int FIND_ERROR_SKIP_LEN = 100;//发现换行符跳过的长度 4G文件是trace1.data是133，trace2.data是131

    //    private static int bucket[] = new int[0X10000];
    private static Buffer buf = new Buffer();


    private static int testTotalCount = 0;//总行数
    public static Set<String> testErrorTraceIdSet = new HashSet<>();//错误traceId
    public static Set<String> testTraceIdSet = new HashSet<>(10000);//所有traceId
    public static Set<Integer> testHashSet = new HashSet<>(10000);//哈希数
    public static int testMinLineLen = 1000;//行的最小长度
    public static int testMaxLineLen = 0;//行的最小长度
    public static Set<String> params = new HashSet<>();

    public static void start() throws Exception {
        long start_time = System.currentTimeMillis();
//        InputStream in = new FileInputStream("/Users/fht/d_disk/chellenger/data/trace1.data");

        String path = "http://127.0.0.1:" + data_port + "/trace" + (Const.who + 1) + ".data ";
        System.out.println(path);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        InputStream in = conn.getInputStream();

        int n, len;
        byte[] data;
        int pageIndex = 0;
        do {
//            pageIndex++;
            data = buf.data;
            //将尾巴复制到缓冲区中
            System.arraycopy(tail, 0, data, 0, tailLen);
            len = tailLen;

            //读取一页数据，
            while ((n = in.read(data, len, Buffer.LEN - len)) != -1) {
                len += n;
                if (len == Buffer.LEN) break;
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
        System.out.println("min line len is :" + testMinLineLen + ", max line len is :" + testMaxLineLen);
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
            i += FIND_CR_SKIP_LEN;
            boolean isError = false;

            for (; ; i++) {
                //可以判断是否小于'='在进去，如果有分支预测技术的话，会增加新能，=和\n成功的次数是20%
                if (data[i] == '=') {
//                count[data[i + 2]]++;
                    //TODO 可以更具字符出现频率，做逻辑上的先后顺序  u2.58 p 2.89 d 3.91
                    //http.status_code=200
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
                    i++;
                    break;
                }
            }
            buf.put(hash, s, i - s);

            if (testMinLineLen > i - s) testMinLineLen = i - s;//统计最小长度
            if (testMaxLineLen < i - s) testMaxLineLen = i - s;//统计最小长度
            if (isError) {
                testErrorTraceIdSet.add(new String(data, s, 16));
//                System.out.print(testErrorTraceIdSet.size() + "\t" + new String(data, s, i - s));

            }
            testTotalCount++;
        } while (i != len);
    }

    private static boolean isError(byte[] d, final int s, int e) {
        for (; e > s; e--) {
            //判断是否有错
            if ((d[e] == '&' || d[e] == '\n') && (d[e - 22] == '&' || d[e - 22] == '|')
                    && (d[e - 4] == '4' || d[e - 4] == '5')
            ) {
//                testErrorTraceIdSet.add(new String(d, s, 16));
//                System.out.print(testErrorTraceIdSet.size() + "\t" + new String(d, i - 25, 25));
//                System.out.println(new String(d, e - 22, 22));
                return true;
            } else if (
                    d[e - 8] == 'e' && d[e - 7] == 'r' &&
                            d[e - 6] == 'r' && d[e - 5] == 'o' &&
                            d[e - 4] == 'r' && d[e - 3] == '=' &&
                            d[e - 2] == '1') {
//                System.out.println(new String(d, i - 9, 9));

//                testErrorTraceIdSet.add(new String(d, s, 16));
//                System.out.print(testErrorTraceIdSet.size() + "\t" + new String(buf, i - 25, 25));
                return true;
            }
        }
        return false;
    }
}
