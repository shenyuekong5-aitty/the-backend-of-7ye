package org.example.springboot2.recommendation.controller;

import org.example.springboot2.recommendation.entity.Recommendation;
import org.example.springboot2.recommendation.service.RecommendationService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(@RequestHeader("token") String token,
                                                      @RequestBody Map<String, Object> payload) {
        String type = (String) payload.get("type");
        Map<String, Object> content = (Map<String, Object>) payload.get("content");
        try {
            recommendationService.submit(token, type, content);
            return ResponseEntity.ok(Map.of("code", 200, "message", "推荐已提交"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("code", 403, "message", e.getMessage()));
        }
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<Map<String, Object>> approve(@RequestHeader("token") String token,
                                                       @PathVariable Long id) {
        try {
            recommendationService.approve(token, id);
            return ResponseEntity.ok(Map.of("code", 200, "message", "审核通过"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("code", 403, "message", e.getMessage()));
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<Map<String, Object>> reject(@RequestHeader("token") String token,
                                                      @PathVariable Long id,
                                                      @RequestBody Map<String, String> body) {
        try {
            recommendationService.reject(token, id, body.get("comment"));
            return ResponseEntity.ok(Map.of("code", 200, "message", "已拒绝"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("code", 403, "message", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingList(@RequestHeader("token") String token) {
        User admin = userService.getUserByToken(token);
        if (admin == null || !"admin".equals(admin.getRole())){
            return ResponseEntity.ok(Map.of("code", 403, "message", "无权限"));
        }
        try {
            List<Map<String, Object>> list = recommendationService.getPendingList();
            return ResponseEntity.ok(Map.of("code", 200, "data", list));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("code", 500, "message", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getList(
            @RequestHeader("token") String token,
            @RequestParam(required = false) String status) {
        // 可在此处添加简单的登录校验（所有用户均可查看）
        try {
            List<Map<String, Object>> list = recommendationService.getListByStatus(status);
            return ResponseEntity.ok(Map.of("code", 200, "data", list));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("code", 500, "message", e.getMessage()));
        }
    }

    /**
     * 获取当前用户的推荐记录
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyRecommendations(@RequestHeader("token") String token) {
        User user = userService.getUserByToken(token);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "未登录"));
        }
        List<Map<String, Object>> list = recommendationService.getListByProposerId(user.getId());
        return ResponseEntity.ok(Map.of("code", 200, "data", list));
    }
}