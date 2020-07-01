package com.aliyun.common;

import java.nio.ByteBuffer;

public class Utils {
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void arraycopy(ByteBuffer bb, int pos, byte[] dst, int offset, int length) {
        int end = offset + length;
        for (int i = 0; i < length; i++)
            dst[offset + i] = bb.get(pos + i);
    }
}
