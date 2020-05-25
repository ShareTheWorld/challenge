package com.aliyun.filter.service;


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