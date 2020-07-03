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
//            preRun();
            if (listen_port == 8001) who = WHO_FILTER_1;
//            data_port = 7000;
//            Data.start();
//            who = WHO_FILTER_1;
//            Data.start();
//            new Thread(() -> Container.handleData()).start();//启动一个创建索引和发现错误的线程
            new Thread(() -> Container.handleErrorPacket()).start();
            filter = new Filter();
            filter.start();
        }
        System.out.println("total run time=" + (System.currentTimeMillis() - startTime));
    }

    public static void preRun() {
//        String str = "3d48bcdc87165ca1|1589285990899211|531955dd01e4f48|69c011804ea644c1|505|Frontend|DoGetConfig|192.168.110.69|db.instance=db&component=java-jdbc&db.type=h2&http.status_code=200&span.kind=client&__sql_id=143cvbw&peer.address=localhost:8082\n";
//        byte bs[] = str.getBytes();
//        Page page = Container.getEmptyPage(0);
//        for (int i = 0; i < 100000; i++) {
//            page.hash(bs, 0);
//        }
//
//        for (int i = 0; i < 100000; i++) {
//            page.getLine(bs, 0);
//        }
//        System.out.println("pre run end");

    }
}
