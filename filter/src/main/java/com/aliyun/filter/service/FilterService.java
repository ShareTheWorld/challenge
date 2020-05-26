package com.aliyun.filter.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class FilterService {
    public void query(HttpServletRequest request, HttpServletResponse response) throws Exception {
        InputStream in = request.getInputStream();
        byte bs[] = new byte[1024];
        int len = in.read(bs);
        if (len != -1) {
            System.out.println(new String(bs, 0, len));
        }
        OutputStream out = response.getOutputStream();
        out.write("abcde".getBytes());
        out.close();
    }
}
