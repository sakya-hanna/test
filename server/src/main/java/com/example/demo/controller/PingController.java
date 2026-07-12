package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PingController {

    @PostMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
            "message", "hello world",
            "time", LocalDateTime.now().toString()
        );
    }
}
