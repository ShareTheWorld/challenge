package com.aliyun.engine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("engine")
public class EngineController {
    @GetMapping("test")
    public String test() {
        return "test";
    }
}
