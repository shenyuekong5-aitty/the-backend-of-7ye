package org.example.springboot2.interaction.view.controller;

import org.example.springboot2.interaction.view.service.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/view")
public class ViewController {
    @Autowired private ViewService viewService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addView(
            @RequestHeader(value = "token", required = false) String token,
            @RequestBody Map<String, String> body) {
        String targetType = body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId"));
        int newCount = viewService.addView(token, targetType, targetId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("viewCount", newCount));
        response.put("message", "浏览量增加成功");
        return ResponseEntity.ok(response);
    }
}