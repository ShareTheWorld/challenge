package com.aliyun.filter.controller;

import com.aliyun.filter.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@RestController
@RequestMapping()
public class FilterController {
    @Autowired
    private FilterService filterService;

    @RequestMapping("/ready")
    public String ready() {
        return "suc";
    }

    @RequestMapping("/setParameter")
    public String setParameter(Integer port) {
        return "suc";
    }

    @RequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long time=System.currentTimeMillis();
        filterService.query(request, response);
        System.out.println(System.currentTimeMillis()-time);
    }
}
