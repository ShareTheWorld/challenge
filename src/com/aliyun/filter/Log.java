package com.aliyun.filter;

/**
 * 表示一条log日志
 */
class Log {
    boolean isError;
    int start;
    int len;

    public Log(int start, int len, boolean isError) {
        this.start = start;
        this.len = len;
        this.isError = isError;
    }
}