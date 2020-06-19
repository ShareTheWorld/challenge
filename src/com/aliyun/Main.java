package com.aliyun;

import com.aliyun.common.Packet;
import com.aliyun.engine.Engine;
import com.aliyun.filter.Container;
import com.aliyun.filter.Data;
import com.aliyun.filter.Filter;
import com.aliyun.filter.Page;

import java.util.Arrays;

public class Main {
    public static final int FILTER_0_PORT = 8000;
    public static final int FILTER_1_PORT = 8001;
    public static final int ENGINE_PORT = 8002;
    public static int listenPort;
    public static byte who = Packet.WHO_FILTER_0;

    public static void main(String args[]) throws Exception {
        listenPort = 8000;
        try {
            listenPort = Integer.valueOf(args[0]);
        } catch (Exception e) {
        }
        long startTime = System.currentTimeMillis();
        System.out.println(Arrays.toString(args));
        if (listenPort == 8002) {
            who = Packet.WHO_ENGINE_2;
            Engine engine = new Engine(listenPort);
            engine.run();
        } else {
//            preheat();
            if (listenPort == 8001) who = Packet.WHO_FILTER_1;
            //init
            Data data = Data.getData();//让data初始化需要的内存空间
            System.out.println("container len is " + Container.class);//为了让Container提前加载进来

            //启动端口监听服务
            Filter filter = new Filter(listenPort);
            filter.run();
        }
        System.out.println("total run time=" + (System.currentTimeMillis() - startTime));
    }

    public static void preheat() {
        Page page = new Page();
        byte[] d = "0000000000000000|1589285990899207|7ef5db0a2d98e3dd|72a620219a1e239|503|LogisticsCenter|db.AlertDao.listByTitleAndUserIdAndFilterStr(..)|192.168.110.67|http.status_code=200&component=java-spring-rest-template&span.kind=client&http.url=http://localhost:9003/getAddress?id=1&peer.port=9003&http.method=GET\n".getBytes();
        for (int i = 0; i < 50000; i++) {
            byte bs[] = page.data;
            System.arraycopy(d, 0, page.data, page.len, d.length);
            page.len += d.length;
        }

        page.createIndex();

        for (int i = 0; i < 50000; i++) {
            page.selectByTraceId("1d48bcdc87165ca2".getBytes());
        }
        System.out.println("end preheat");
    }
}
