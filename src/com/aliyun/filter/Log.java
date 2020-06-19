package com.aliyun.filter;

/**
 * 表示一条log日志
 */
class Log {
    int start;
    int len;

    public Log(int start, int len) {
        this.start = start;
        this.len = len;
    }
}