package com.aliyun;

import com.aliyun.common.Packet;
import com.aliyun.engine.Engine;
import com.aliyun.filter.Container;
import com.aliyun.filter.Data;
import com.aliyun.filter.Filter;
import com.aliyun.filter.Page;

import static com.aliyun.common.Const.*;

import java.util.Arrays;

public class Main {

    public static void main(String args[]) throws Exception {
        listen_port = 8000;
        try {
            listen_port = Integer.valueOf(args[0]);
        } catch (Exception e) {
        }
        long startTime = System.currentTimeMillis();
        System.out.println(Arrays.toString(args));
        if (listen_port == 8002) {
            who = WHO_ENGINE;
            new Engine().start();
        } else {
            if (listen_port == 8001) who = WHO_FILTER_1;
//            data_port = 7000;
//            Data.start();
//            who = WHO_FILTER_1;
//            Data.start();
            new Thread(() -> Container.handleErrorPacket()).start();//启动一个处理错误的线程
            filter = new Filter();
            filter.start();
        }
        System.out.println("total run time=" + (System.currentTimeMillis() - startTime));
    }
}
