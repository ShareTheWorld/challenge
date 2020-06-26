package com.aliyun.common;

import com.aliyun.filter.Filter;

public class Const {
    public static final int WHO_FILTER_0 = 0;
    public static final int WHO_FILTER_1 = 1;
    public static final int WHO_ENGINE = 2;

    public static final int FILTER_0_LISTEN_PORT = 8000;
    public static final int FILTER_1_LISTEN_PORT = 8001;
    public static final int ENGINE_LISTEN_PORT = 8002;

    public static int listen_port = 8000;//监听端口
    public static int data_port = 0;//数据端口
    public static byte who = WHO_FILTER_0;//代表是那个节点

    public static Filter filter;

    //两核加上超线程技术，可以同时执行4个线程
    public static final int PER_HANDLE_PAGE_NUM = 4;//表示每次处理多少页数据，必须小于读取数据缓存页的长度-1

    public static int total_page_count = 10000;//表示总页数，当真正的页数被计算出来过后会赋值给他


}
