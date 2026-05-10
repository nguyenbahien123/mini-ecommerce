package vn.nbh.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/general")
    public Mono<ResponseEntity<Map<String, Object>>> generalFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 503);
        response.put("message", "Hệ thống đang quá tải hoặc dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau ít phút!");
        response.put("result", null);

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}