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

    @PostMapping("/goodbye")
    public Map<String, Object> goodbye() {
        return Map.of(
            "message", "goodbye world",
            "status", "success",
            "code", 200,
            "time", LocalDateTime.now().toString()
        );
    }
}
