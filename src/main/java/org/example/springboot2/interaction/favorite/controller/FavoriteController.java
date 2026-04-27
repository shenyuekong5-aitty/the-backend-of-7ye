package org.example.springboot2.favorite.controller;

import org.example.springboot2.favorite.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @RequestHeader("token") String token,
            @RequestBody Map<String, Object> body) {
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        try {
            favoriteService.addFavorite(token, targetType, targetId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "收藏成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @RequestHeader("token") String token,
            @RequestBody Map<String, Object> body) {
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        try {
            favoriteService.removeFavorite(token, targetType, targetId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "已取消收藏");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 切换收藏状态（智能收藏/取消）
     */
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestHeader("token") String token,
            @RequestBody Map<String, Object> body) {
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        try {
            boolean isFavorited = favoriteService.toggleFavorite(token, targetType, targetId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", Map.of("isFavorited", isFavorited));
            response.put("message", isFavorited ? "收藏成功" : "已取消收藏");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkFavorite(
            @RequestHeader("token") String token,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        boolean favorited = favoriteService.isFavorited(token, targetType, targetId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("isFavorited", favorited));
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户的收藏列表
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyFavorites(
            @RequestHeader("token") String token,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Map<String, Object> data = favoriteService.getUserFavorites(token, pageNo, pageSize);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}