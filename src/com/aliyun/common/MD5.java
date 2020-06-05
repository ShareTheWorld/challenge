package com.aliyun.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    private static final byte[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static MessageDigest md;

    public static void main(String args[]) {
        MD5 md5 = new MD5();
        byte bs[] = new byte[]{'a', 'b', 'c', 'd', 'e'};
        md5.update(bs, 1, 3);
        byte result[] = new byte[32];
        md5.digest(result, 0);
        System.out.println(new String(result));
    }

    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public MD5() {
        md.reset();
    }

    public void reset() {
        md.reset();
    }

    public MD5 update(byte[] bs, int start, int len) {
        try {
            md.update(bs, start, len);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }


    public void digest(byte bs[], int start) {
        byte[] md = MD5.md.digest();
        for (int i = 0; i < 16; i++) {
            bs[start++] = HEX_CHAR[(md[i] & 0XFF) >> 4];
            bs[start++] = HEX_CHAR[md[i] & 0XF];
        }
    }
}
