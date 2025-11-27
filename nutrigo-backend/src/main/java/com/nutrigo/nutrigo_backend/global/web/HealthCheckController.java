package com.nutrigo.nutrigo_backend.global.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/health")
    public String ok() {
        return "ok";
    }

}
