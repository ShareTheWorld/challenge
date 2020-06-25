package com.aliyun.filter;

import com.aliyun.common.Const;
import com.aliyun.common.Packet;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.aliyun.common.Const.*;

public class Data {
    //8M 4万条，去掉重复过后是2100条无重复的traceId
//    private static byte[] buf = new byte[8 * 1024 * 1024];//存放数据的缓冲区，太大了会导致缓存页不停的失效
    private static byte[] tail = new byte[1024];//尾巴数据
    private static int tailLen = 0;//尾巴数据的长度

    private static int FIND_CR_SKIP_LEN = 100;//发现换行符跳过的长度 4G文件是trace1.data是133，trace2.data是131
    private static int FIND_ERROR_SKIP_LEN = 100;//发现换行符跳过的长度 4G文件是trace1.data是133，trace2.data是131

    //    private static int bucket[] = new int[0X10000];
//    private static Buffer pre = new Buffer();//上一个
//    private static Buffer buf = new Buffer();//当前
    private static int page;//表示多少页

    private static int testTotalCount = 0;//总行数
    public static Set<String> testErrorTraceIdSet = new HashSet<>();//错误traceId
    public static Set<String> testTraceIdSet = new HashSet<>(10000);//所有traceId
    public static Set<Integer> testHashSet = new HashSet<>(10000);//哈希数
    public static int testMinLineLen = 1000;//行的最小长度
    public static int testMaxLineLen = 0;//行的最小长度
    public static Set<String> testParams = new HashSet<>();

    public static void start() {
        try {
            start0();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void start0() throws Exception {
        long start_time = System.currentTimeMillis();
//        InputStream in = new FileInputStream("/Users/fht/d_disk/chellenger/data/trace1.data");

        String path = "http://127.0.0.1:" + data_port + "/trace" + (Const.who + 1) + ".data ";
        System.out.println(path);
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        InputStream in = conn.getInputStream();

        int n, len;
        byte[] data;
        Buffer buf;
        do {
//            System.out.println("read data, page =" + page);
            //获取一个buf
            buf = Container.get(page);
            buf.clear();
            buf.setPage(page);

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

            //计算长度
            buf.len = len - tailLen;

            asyncHandleData(buf);//异步处理数据

            page++;
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
        System.out.println("page count=" + page);
        System.out.println("params size=" + testParams.size());
        System.out.println(testParams);
    }

    public static void asyncHandleData(Buffer buf) {
        new Thread(() -> handleData(buf)).start();
    }

    private static void handleData(Buffer buf) {
        byte[] data = buf.data;
        int i = 0;
        do {
            //计算索引
            int hash = (data[i] + (data[i + 1] << 3) + (data[i + 2] << 6) + (data[i + 3] << 9) + (data[i + 4] << 12)) & 0XFFFF;
//            testHashSet.add(index);

            //获取一行数据
            int l = getLine(data, i, buf.errPkt);
            buf.put(hash, i, l);
            i = i + l;

            if (testMinLineLen > l) testMinLineLen = l;//统计最小长度
            if (testMaxLineLen < l) testMaxLineLen = l;//统计最小长度
            testTotalCount++;
        } while (i != buf.len);

        //发送错误的traceId到engine
//        filter.sendPacket(buf.packet);

        //处理完了，将数据的状态设置为1
        Container.finishIndex(buf);//完成索引创建
    }

    public static int getLine(byte d[], int s, Packet packet) {
        int i = s;
        i += FIND_CR_SKIP_LEN;
        boolean isError = false;
        for (; ; i++) {
            //可以判断是否小于'='在进去，如果有分支预测技术的话，会增加新能，=和\n成功的次数是20%
            if (d[i] == '=') {
                //TODO 可以更具字符出现频率，做逻辑上的先后顺序  u2.58 p 2.89 d 3.91
                if (d[i - 5] == '_' && (d[i + 1] != '2' || d[i + 2] != '0' || d[i + 3] != '0')
                        && d[i - 16] == 'h' && d[i - 15] == 't' && d[i - 14] == 't' && d[i - 13] == 'p'
                        && d[i - 12] == '.' && d[i - 11] == 's' && d[i - 10] == 't' && d[i - 9] == 'a'
                        && d[i - 8] == 't' && d[i - 7] == 'u' && d[i - 6] == 's'
                        && d[i - 4] == 'c' && d[i - 3] == 'o' && d[i - 2] == 'd' && d[i - 1] == 'e') {
                    isError = true;
                } else if (d[i + 1] == '1' && d[i - 5] == 'e' && d[i - 4] == 'r' && d[i - 3] == 'r' && d[i - 2] == 'o'
                        && d[i - 1] == 'r') {
                    isError = true;
                }
            } else if (d[i] == '\n') {
                i++;
                break;
            }
        }
        if (isError) {
            packet.write(d, s, 16);
//            System.out.print(testErrorTraceIdSet.size() + "\t" + new String(data, s, i - s));
        }
        return i - s;
    }
}
