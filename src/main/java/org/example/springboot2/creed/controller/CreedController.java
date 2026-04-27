package org.example.springboot2.creed.controller;

import org.example.springboot2.creed.entity.Creed;
import org.example.springboot2.creed.service.CreedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/creed")
public class CreedController {

    @Autowired
    private CreedService creedService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getCreedList() {
        List<Creed> creeds = creedService.getAllCreeds();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", creeds);
        data.put("message", "获取信条列表成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}