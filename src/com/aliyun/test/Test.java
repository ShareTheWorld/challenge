package com.aliyun.test;

public class Test {
    static byte[] bs = new byte[32 * 1024 * 1024];
    static int len = 0;

    public static void main(String args[]) {

        String str = "462772bc64b21b93|1592841025854464|462772bc64b21b93|0|1283|OrderCenter|DoGetTProfInteractionSnapshot|192.168.133.141|biz=fxtius&sampler.type=const&sampler.param=1\n";
        byte d[] = str.getBytes();
        for (int i = 0; i < bs.length / d.length; i++) {
            System.arraycopy(d, 0, bs, len, d.length);
            len += d.length;
        }
        long startTime = System.currentTimeMillis();
        int a = 0;
//        for (int i = 0; i < 30000000; i++) {
//            if (bs[i] == '%') break;
//        }
        System.out.println("time=" + (System.currentTimeMillis() - startTime) + "  " + a);
        test(bs);
        System.out.println("time=" + (System.currentTimeMillis() - startTime));

    }

    public static void test(byte data[]) {
        long start_time = System.currentTimeMillis();
        int i = 0;
        do {
            int hash = hash(data, i);
            //获取一行数据
            int l = getLine(data, i);
//            put(hash, i, l);
            i = i + l;
        } while (i != len);//如果恰好等于的话，就说明刚好到达最后了,这样getLog就不需要进行边界判断了
        System.out.println("create index and find error, time=" + (System.currentTimeMillis() - start_time));

    }

    public static void isError(byte d[], int s, int i) {
        //TODO 可以更具字符出现频率，做逻辑上的先后顺序  u2.58 p 2.89 d 3.91
        if (d[i - 16] == 'h' && d[i - 15] == 't' && d[i - 14] == 't' && d[i - 13] == 'p'
//                && d[i - 12] == '.' && d[i - 11] == 's' && d[i - 10] == 't' && d[i - 9] == 'a'
//                && d[i - 8] == 't' && d[i - 7] == 'u' && d[i - 6] == 's' && d[i - 5] == '_'
                && d[i - 4] == 'c' && d[i - 3] == 'o' && d[i - 2] == 'd' && d[i - 1] == 'e') {
            if (d[i + 1] != '2' || d[i + 2] != '0' || d[i + 3] != '0') {
            }
            return;
        } else if (d[i - 5] == 'e' && d[i - 4] == 'r' && d[i - 3] == 'r' && d[i - 2] == 'o'
                && d[i - 1] == 'r') {
            if (d[i + 1] == '1') {
            }
            return;
        }
    }

    public static int hash(byte[] d, int i) {
        return (d[i] + (d[i + 1] << 3) + (d[i + 2] << 6) + (d[i + 3] << 9) + (d[i + 4] << 12)) & 0XFFFF;
    }

    public static int getLine(byte[] d, int s) {
        int i = s + 100;
        //开始寻早error=1和!http.status_code=200 和\n
        while (d[i++] != '\n') {
            if (d[i] == '=') {
                isError(d, s, i);
            }
        }
        return i - s;
    }
}
