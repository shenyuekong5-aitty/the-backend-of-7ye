package org.example.springboot2.quote.controller;

import org.example.springboot2.quote.entity.Quote;
import org.example.springboot2.quote.service.QuoteService;
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
@RequestMapping("/api/quote")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private UserService userService;

    /**
     * 获取所有语录（公开）
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getList() {
        List<Quote> quotes = quoteService.getAllQuotes();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("items", quotes));
        response.put("message", "获取成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 新增语录（仅管理员）
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addQuote(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("code", 403, "message", "无权限"));
        }
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "内容不能为空"));
        }
        Quote saved = quoteService.addQuote(content);
        return ResponseEntity.ok(Map.of("code", 200, "data", saved, "message", "添加成功"));
    }

    /**
     * 修改语录（仅管理员）
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateQuote(
            @RequestHeader("token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("code", 403, "message", "无权限"));
        }
        String newContent = body.get("content");
        if (newContent == null || newContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "内容不能为空"));
        }
        try {
            Quote updated = quoteService.updateQuote(id, newContent);
            return ResponseEntity.ok(Map.of("code", 200, "data", updated, "message", "修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", 404, "message", e.getMessage()));
        }
    }

    /**
     * 删除语录（仅管理员）
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuote(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("code", 403, "message", "无权限"));
        }
        try {
            quoteService.deleteQuote(id);
            return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", 404, "message", e.getMessage()));
        }
    }
}