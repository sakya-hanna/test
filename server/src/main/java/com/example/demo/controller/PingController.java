package com.example.demo.controller;

import com.example.demo.model.PingLog;
import com.example.demo.model.PingLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PingController {

    private final PingLogRepository repo;

    public PingController(PingLogRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/ping")
    public Map<String, Object> ping(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "ping");
        PingLog log = repo.save(new PingLog(content));

        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("id", log.getId());
        result.put("content", log.getContent());
        result.put("createdAt", log.getCreatedAt().toString());
        return result;
    }

    @GetMapping("/ping")
    public List<PingLog> list() {
        return repo.findAll();
    }
}
