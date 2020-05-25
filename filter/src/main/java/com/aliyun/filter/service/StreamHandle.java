package com.aliyun.filter.service;


import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StreamHandle {
    public static final int PER_READ_LEN = 256 * 1024;//每次读取长度
    private InputStream in;
    private List list = new ArrayList(1000);

    public static void main(String args[]) throws Exception {
        long startTime = System.currentTimeMillis();
//            new StreamHandle(new FileInputStream("/home/fu/Desktop/challege/" + "trace1.data"));
//            URL url1 = new URL("http://127.0.0.1:8000/trace1.data");
//            HttpURLConnection httpConnection1 = (HttpURLConnection) url1.openConnection(Proxy.NO_PROXY);
//            InputStream input1 = httpConnection1.getInputStream();
//            new StreamHandle(input1);
//
//            URL url2 = new URL("http://127.0.0.1:8000/trace2.data");
//            HttpURLConnection httpConnection2 = (HttpURLConnection) url2.openConnection(Proxy.NO_PROXY);
//            InputStream input2 = httpConnection2.getInputStream();
//            new StreamHandle(input2);

//            String path = "/root/chellenge/";
        String path = "/Users/fht/d_disk/chellenger/data/";
        new StreamHandle(new FileInputStream(path + "trace1.data"));
        new StreamHandle(new FileInputStream(path + "trace2.data"));
        System.out.println("total time=" + (System.currentTimeMillis() - startTime));
        System.out.println("errSet.size()=" + Page.errSet.size());
        System.out.println("logMinLength=" + Page.logMinLength);
        System.out.println("testLineNumber=" + Page.testLineNumber);


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
        Page page = new Page();
        long totalCount = 0;
        while (true) {
            //保证读取的一页接近1M，
            int len;
            while ((len = in.read(page.data, page.len, Math.min(PER_READ_LEN, page.data.length - page.len))) != -1) {
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
            Page t_page = page;
//            new Thread(() -> {
            t_page.createIndex();
//            }).start();
//            System.out.println(new String(page.data, 0, page.len));
//            System.out.println();
//            System.out.println("Page " + (++count) + "  " + page.len + "  " + (char) (page.data[page.len - 1]));
            totalCount += page.len;
            page = newPage;
            //寻找换行符
            if (len == -1) break;
        }
        System.out.println("totalCount=" + totalCount);


    }

    public List<String> selectByTraceId() {

        return null;
    }
}
