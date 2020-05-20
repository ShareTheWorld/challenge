package com.aliyun.filter.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class StreamHandle {
    private InputStream in;

    public static void main(String args[]) throws FileNotFoundException {
        new StreamHandle(new FileInputStream("/Users/fht/d_disk/chellenger/data/" + "trace1.data"));
    }

    public StreamHandle(InputStream in) {
        this.in = in;
        try {
            long startTime = System.currentTimeMillis();
            this.start();
            System.out.println(System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        int count = 0;
        Page page = new Page();
        int totalCount = 0;
        while (true) {
            //保证读取的一页接近1M，
            int len;
            while (true) {
                len = in.read(page.buffer, page.len, page.buffer.length - page.len);//一次尝试读取1M数据
                if (len == -1) break;
                page.len += len;
                if (page.len > Page.min) break;
            }

            Page newPage = new Page();//添加一个新的page
            for (int i = page.len - 1; i >= 0; i--) {
                if ('\n' == page.buffer[i]) {
                    int l = page.len - i - 1;
                    System.arraycopy(page.buffer, i + 1, newPage.buffer, 0, l);
                    page.len = page.len - l;
                    newPage.len = l;
                    break;
                }
            }
            //将这一页码
//            System.out.println("Page " + (++count) + "  " + page.len + "  " + (char) (page.buffer[page.len - 1]));
            totalCount += page.len;
            page = newPage;
            //寻找换行符
            if (len == -1) break;
        }
        System.out.println(totalCount);


    }

    public List<String> selectByTraceId() {

        return null;
    }
}

class Page {
    public byte[] buffer = new byte[1024 * 1024];//用于存放数据
    public static int min = 1022 * 1024;//要求读数据的最小长度
    public int len;//用于存放数据的长度
    //下面是建立索引的字段
}