package com.aliyun.filter.fegin;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

public class DataInput {
    private static String path = "/Users/fht/d_disk/chellenger/data/";
//    private static String path = "/home/fu/Desktop/challege/";

    public static InputStream getInputStream(int port) {
        try {
            InputStream in;
            if (port == 8001) {
                in = new FileInputStream(path + "trace1.data");
            } else {
                in = new FileInputStream(path + "trace2");
            }
            return in;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
