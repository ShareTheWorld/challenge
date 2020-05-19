package com.aliyun.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

@RestController
@RequestMapping()
public class EngineController {
    @Autowired
    HttpServletRequest req;
    @GetMapping("ready")
    public String ready() {
        return "success";
    }

    @GetMapping("setParameter")
    public String setParameter(Integer port) throws Exception{
        System.out.print(port);
        return "success";
    }

    @RequestMapping("/start")
    public String start() {
        return "suc";
    }

}
