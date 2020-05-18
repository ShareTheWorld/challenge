package com.aliyun.filter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("filter")
public class FilterController {
    @GetMapping("test")
    public String test() {
        return "test";
    }
}
