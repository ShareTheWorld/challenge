package com.aliyun.test;

public class Test {
    public static void main(String args[]) throws Exception {
        byte bs[] = new byte[1024 * 1024 * 32];

        long startTime = System.currentTimeMillis();
        long l = 0;
        for (int i = 0; i < 32 * 1024 * 1024; i++) {
            if (bs[i] == 1) break;
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }
}
