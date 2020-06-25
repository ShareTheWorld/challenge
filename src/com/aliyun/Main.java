package com.aliyun;

import com.aliyun.engine.Engine;
import com.aliyun.filter.Data;
import com.aliyun.filter.Filter;

import static com.aliyun.common.Const.*;

public class Main {
    public static void main(String args[]) throws Exception {
        System.out.println("load Data " + Data.class);
        long startTime = System.currentTimeMillis();
        listen_port = 8000;
        try {
            listen_port = Integer.valueOf(args[0]);
        } catch (Exception e) {
        }
        if (listen_port == 8002) {
            who = WHO_ENGINE;
            new Engine().start();
        } else {
            if (listen_port == 8001) who = WHO_FILTER_1;
            data_port = 7000;
            Data.start();
            who = WHO_FILTER_1;
            Data.start();
            Filter filter = new Filter();
            filter.start();
        }
        System.out.println("total run time=" + (System.currentTimeMillis() - startTime));
    }
}
