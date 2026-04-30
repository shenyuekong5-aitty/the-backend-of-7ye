package org.example.springboot2.music.controller;

import org.example.springboot2.music.entity.Music;
import org.example.springboot2.music.service.MusicService;
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
@RequestMapping("/api/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getMusicList() {
        List<Music> musics = musicService.getAllMusics();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", musics);
        data.put("message", "获取音乐列表成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMusic(@RequestHeader(value = "token", required = false) String token,
                                                        @RequestBody Music music) {
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        try {
            Music saved = musicService.addMusic(music);
            return ResponseEntity.ok(successResponse(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("新增失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateMusic(@RequestHeader(value = "token", required = false) String token,
                                                           @RequestBody Music music) {
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        try {
            Music updated = musicService.updateMusic(music);
            return ResponseEntity.ok(successResponse(updated));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("编辑失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteMusic(@RequestHeader(value = "token", required = false) String token,
                                                           @PathVariable Long id) {
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        try {
            musicService.deleteMusic(id);
            return ResponseEntity.ok(successResponse(null));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("删除失败: " + e.getMessage());
        }
    }

    private boolean isAdmin(String token) {
        User user = userService.getUserByToken(token);
        return user != null && "admin".equals(user.getRole());
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