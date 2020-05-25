package com.aliyun.filter;

import com.aliyun.filter.service.StreamHandle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.stream.Stream;

@SpringBootApplication
public class FilterApplication {
    public static void main(String args[]) throws Exception {
        StreamHandle.main(null);
        SpringApplication.run(FilterApplication.class, args);
    }
}
