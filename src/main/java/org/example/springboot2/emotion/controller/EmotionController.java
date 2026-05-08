package org.example.springboot2.emotion.controller;

import org.example.springboot2.emotion.entity.Emotion;
import org.example.springboot2.emotion.service.EmotionService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emotion")
public class EmotionController {

    @Autowired
    private EmotionService emotionService;

    @Autowired
    private UserService userService;

    // ---------- 辅助响应方法 ----------
    private ResponseEntity<Map<String, Object>> unauthorized() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 401);
        resp.put("message", "未登录或token无效");
        return ResponseEntity.status(401).body(resp);
    }

    private ResponseEntity<Map<String, Object>> notFound() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 404);
        resp.put("message", "情绪不存在");
        return ResponseEntity.status(404).body(resp);
    }

    private ResponseEntity<Map<String, Object>> forbidden() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 403);
        resp.put("message", "无权限操作");
        return ResponseEntity.status(403).body(resp);
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Object data) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("message", message);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    // ---------- 业务接口 ----------

    /**
     * 获取情绪列表（公开，无需登录）
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getList() {
        List<Map<String, Object>> items = emotionService.getAllEmotionsWithDetails();
        return ok("获取成功", Map.of("items", items));
    }

    /**
     * 新增情绪
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null) return unauthorized();

        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 400);
            resp.put("message", "内容不能为空");
            return ResponseEntity.badRequest().body(resp);
        }

        String time = body.getOrDefault("time", LocalDateTime.now().toString());
        Emotion saved = emotionService.addEmotion(user.getId(), content, time);
        return ok("发表成功", saved);
    }

    /**
     * 修改情绪（仅作者或管理员）
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @RequestHeader("token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null) return unauthorized();

        Emotion emotion = emotionService.getById(id);
        if (emotion == null) return notFound();

        // 权限校验：管理员或作者本人
        if (!user.getId().equals(emotion.getUserid()) && !"admin".equals(user.getRole())) {
            return forbidden();
        }

        String newContent = body.get("content");
        if (newContent == null || newContent.trim().isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 400);
            resp.put("message", "内容不能为空");
            return ResponseEntity.badRequest().body(resp);
        }

        Emotion updated = emotionService.updateContent(id, newContent);
        return ok("修改成功", updated);
    }

    /**
     * 删除情绪（仅作者或管理员）
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        User user = userService.getUserByToken(token);
        if (user == null) return unauthorized();

        Emotion emotion = emotionService.getById(id);
        if (emotion == null) return notFound();

        if (!user.getId().equals(emotion.getUserid()) && !"admin".equals(user.getRole())) {
            return forbidden();
        }

        emotionService.deleteEmotion(id);
        return ok("删除成功", null);
    }

    /**
     * 追加回复（remarks）
     */
    @PostMapping("/reply/{id}")
    public ResponseEntity<Map<String, Object>> addReply(
            @RequestHeader("token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null) return unauthorized();

        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 400);
            resp.put("message", "回复内容不能为空");
            return ResponseEntity.badRequest().body(resp);
        }

        Emotion updated = emotionService.addReply(id, content);
        return ok("回复成功", updated);
    }
}