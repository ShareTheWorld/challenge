package com.aliyun.test;

import java.io.FileInputStream;
import java.io.InputStream;

public class Test {
    public static void main(String args[]) throws Exception {
        String path = "/home/fu/Desktop/challege/data";
        InputStream in = new FileInputStream(path + "/trace1.data");
        byte b[] = new byte[1024 * 1024];
        int len = 0;
        while ((len = in.read(b)) != -1) {

        }

        len = in.read(b);
        len = in.read(b);
        len = in.read(b);

    }
}
