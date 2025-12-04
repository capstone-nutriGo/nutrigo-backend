package com.nutrigo.nutrigo_backend.global.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/health")
    /*
        #swagger.summary = '헬스 체크'
        #swagger.description = '배포 상태 확인용 단순 응답을 반환합니다.'
    */
    public String ok() {
        return "ok";
    }

}
