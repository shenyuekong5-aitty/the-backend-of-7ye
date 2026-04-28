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

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCognize(
            @RequestHeader("token") String token,
            @RequestBody Cognize cognize) {
        try {
            Cognize saved = cognizeService.addCognize(token, cognize);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", saved);
            response.put("message", "创建成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCognize(
            @RequestHeader("token") String token,
            @RequestBody Cognize cognize) {
        try {
            Cognize updated = cognizeService.updateCognize(token, cognize);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", updated);
            response.put("message", "更新成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteCognize(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        try {
            cognizeService.deleteCognize(token, id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}