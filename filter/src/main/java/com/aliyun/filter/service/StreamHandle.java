package com.aliyun.filter.service;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StreamHandle {
    public static final int PER_READ_LEN = 256 * 1024;//每次读取长度
    private InputStream in;
    private List list = new ArrayList(1000);

    public static void main(String args[]) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            new StreamHandle(new FileInputStream("/home/fu/Desktop/challege/" + "trace1.data"));
        }
        System.out.println(System.currentTimeMillis() - startTime);

    }

    public StreamHandle(InputStream in) {
        this.in = in;
        try {
            this.start();
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
                len = in.read(page.data, page.len, Math.min(PER_READ_LEN, page.data.length - page.len));//一次尝试读取1M数据
                if (len == -1) break;
                page.len += len;
                if (page.len > Page.min) break;
            }

            Page newPage = new Page();//添加一个新的page
            for (int i = page.len - 1; i >= 0; i--) {
                if ('\n' == page.data[i]) {
                    int l = page.len - i - 1;
                    System.arraycopy(page.data, i + 1, newPage.data, 0, l);
                    page.len = page.len - l;
                    newPage.len = l;
                    break;
                }
            }
            //将这一页码
//            list.add(page);
            page.createIndex();
//            new String(page.data, 0, page.len);
//            System.out.println();
//            System.out.println("Page " + (++count) + "  " + page.len + "  " + (char) (page.data[page.len - 1]));
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
