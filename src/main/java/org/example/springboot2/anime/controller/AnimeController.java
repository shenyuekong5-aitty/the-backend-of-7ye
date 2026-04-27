package org.example.springboot2.anime.controller;

import org.example.springboot2.anime.entity.Anime;
import org.example.springboot2.anime.service.AnimeService;
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
@RequestMapping("/api/anime")
public class AnimeController {

    @Autowired
    private AnimeService animeService;

    @Autowired
    private UserService userService;

    // 获取番剧列表（无需权限）
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAnimeList() {
        List<Anime> animes = animeService.getAllAnimes();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", animes);
        data.put("message", "获取番剧列表成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    // 新增番剧（仅管理员）
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAnime(@RequestHeader(value = "token", required = false) String token,
                                                        @RequestBody Anime anime) {
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        try {
            Anime saved = animeService.addAnime(anime);
            return ResponseEntity.ok(successResponse(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("新增失败: " + e.getMessage());
        }
    }

    // 编辑番剧（仅管理员）
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateAnime(@RequestHeader(value = "token", required = false) String token,
                                                           @RequestBody Anime anime) {
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        try {
            Anime updated = animeService.updateAnime(anime);
            return ResponseEntity.ok(successResponse(updated));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("编辑失败: " + e.getMessage());
        }
    }

    // 删除番剧（仅管理员）
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnime(@RequestHeader(value = "token", required = false) String token,
                                                           @PathVariable Long id) {
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        try {
            animeService.deleteAnime(id);
            return ResponseEntity.ok(successResponse(null));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("删除失败: " + e.getMessage());
        }
    }

    private boolean isAdmin(String token) {
        User user = userService.getUserByToken(token);
        return user != null && user.getRoles() != null && user.getRoles().contains("admin");
    }

    private ResponseEntity<Map<String, Object>> buildMissingTokenResponse() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 401);
        resp.put("message", "缺少认证 Token");
        resp.put("data", null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }

    private ResponseEntity<Map<String, Object>> buildUnauthorizedResponse() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 403);
        resp.put("message", "无权限执行此操作");
        resp.put("data", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
    }

    private Map<String, Object> successResponse(Object data) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("message", "操作成功");
        resp.put("data", data);
        return resp;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 500);
        resp.put("message", message);
        resp.put("data", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}