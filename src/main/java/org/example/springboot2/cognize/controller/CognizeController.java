package org.example.springboot2.cognize.controller;

import org.example.springboot2.cognize.entity.Cognize;
import org.example.springboot2.cognize.service.CognizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cognize")
public class CognizeController {

    @Autowired
    private CognizeService cognizeService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getCognizeList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = cognizeService.getCognizeList(pageNo, pageSize);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", data);
        response.put("message", "获取认知列表成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCognizeById(@PathVariable Long id) {
        Cognize cognize = cognizeService.getCognizeById(id);
        Map<String, Object> response = new HashMap<>();
        if (cognize == null) {
            response.put("code", 404);
            response.put("message", "条目不存在");
            return ResponseEntity.ok(response);
        }
        response.put("code", 200);
        response.put("data", cognize);
        return ResponseEntity.ok(response);
    }
}