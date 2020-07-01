package com.aliyun.filter;

import java.nio.ByteBuffer;

public class Log {
    public ByteBuffer d;
    public int s;
    public int l;

    public Log(ByteBuffer d, int s, int l) {
        this.d = d;
        this.s = s;
        this.l = l;
    }
}
