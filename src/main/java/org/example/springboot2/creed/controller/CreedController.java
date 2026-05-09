package org.example.springboot2.creed.controller;

import org.example.springboot2.creed.entity.Creed;
import org.example.springboot2.creed.service.CreedService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/creed")
public class CreedController {

    @Autowired
    private CreedService creedService;

    @Autowired
    private UserService userService;

    // 获取列表（公开）
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getList() {
        List<Creed> creeds = creedService.getAllCreeds();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("items", creeds));
        response.put("message", "获取成功");
        return ResponseEntity.ok(response);
    }

    // 新增（仅 admin）
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCreed(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", 400, "message", "内容不能为空"));
        }
        Creed saved = creedService.addCreed(content);
        return ResponseEntity.ok(Map.of("code", 200, "data", saved, "message", "添加成功"));
    }

    // 修改（仅 admin）
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateCreed(
            @RequestHeader("token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        String newContent = body.get("content");
        if (newContent == null || newContent.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", 400, "message", "内容不能为空"));
        }
        try {
            Creed updated = creedService.updateCreed(id, newContent);
            return ResponseEntity.ok(Map.of("code", 200, "data", updated, "message", "修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }

    // 删除（仅 admin）
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteCreed(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        try {
            creedService.deleteCreed(id);
            return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }
}