package com.aliyun.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
