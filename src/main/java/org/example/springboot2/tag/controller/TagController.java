package org.example.springboot2.tag.controller;

import org.example.springboot2.tag.entity.Tag;
import org.example.springboot2.tag.service.TagService;
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
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    // 获取所有标签（公开）
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getList() {
        List<Tag> tags = tagService.getAllTags();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("items", tags));
        response.put("message", "获取成功");
        return ResponseEntity.ok(response);
    }

    // 新增标签（仅管理员）
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addTag(
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
        Tag saved = tagService.addTag(content);
        return ResponseEntity.ok(Map.of("code", 200, "data", saved, "message", "添加成功"));
    }

    // 修改标签（仅管理员）
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateTag(
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
            Tag updated = tagService.updateTag(id, newContent);
            return ResponseEntity.ok(Map.of("code", 200, "data", updated, "message", "修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }

    // 删除标签（仅管理员）
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteTag(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        try {
            tagService.deleteTag(id);
            return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }
}