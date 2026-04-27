package org.example.springboot2.nickname.controller;

import org.example.springboot2.nickname.service.NicknameService;
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
@RequestMapping("/api/user")
public class NicknameController {

    @Autowired
    private NicknameService nicknameService;

    @Autowired
    private UserService userService;   // ✅ 注入用户服务，用于权限验证

    // 1. 查询昵称列表（无需权限）
    @GetMapping("/nicknames")
    public ResponseEntity<Map<String, Object>> getNicknames(@RequestParam(required = false) String keyword) {
        List<String> nicknames = nicknameService.getNicknames(keyword);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("nicknames", nicknames);
        data.put("message", keyword != null ? "搜索 \"" + keyword + "\" 成功" : "获取成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    // 2. 新增昵称（仅管理员）
    @PostMapping("/nickname/add")
    public ResponseEntity<Map<String, Object>> addNickname(
            @RequestHeader(value = "token", required = false) String token,
            @RequestBody Map<String, String> body) {
        // 权限校验
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        String newName = body.get("newName");
        boolean success = nicknameService.addNickname(newName);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("code", 200);
            response.put("data", Map.of("message", "新增成功"));
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 201);
            response.put("data", Map.of("message", "昵称已存在"));
            return ResponseEntity.ok(response);
        }
    }

    // 3. 修改昵称（仅管理员）
    @PutMapping("/nickname/update")
    public ResponseEntity<Map<String, Object>> updateNickname(
            @RequestHeader(value = "token", required = false) String token,
            @RequestBody Map<String, String> body) {
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        String oldName = body.get("oldName");
        String newName = body.get("newName");
        boolean success = nicknameService.updateNickname(oldName, newName);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("code", 200);
            response.put("data", Map.of("message", "修改成功"));
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 201);
            response.put("data", Map.of("message", "原昵称不存在或新昵称已存在"));
            return ResponseEntity.ok(response);
        }
    }

    // 4. 删除昵称（仅管理员）
    @DeleteMapping("/nickname/delete")
    public ResponseEntity<Map<String, Object>> deleteNickname(
            @RequestHeader(value = "token", required = false) String token,
            @RequestParam String name) {
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        boolean success = nicknameService.deleteNickname(name);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("code", 200);
            response.put("data", Map.of("message", "删除成功"));
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 201);
            response.put("data", Map.of("message", "昵称不存在"));
            return ResponseEntity.ok(response);
        }
    }

    // ✅ 权限验证辅助方法
    private boolean isAdmin(String token) {
        if (token == null) return false;
        User user = userService.getUserByToken(token);
        if (user == null) return false;
        String roles = user.getRoles();
        // roles 可能是 "admin" 或 "[\"admin\"]"，简单判断包含即可
        return roles != null && roles.contains("admin");
    }

    // ✅ 构建未授权响应
    private ResponseEntity<Map<String, Object>> buildUnauthorizedResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 403);
        response.put("message", "无权限执行此操作");
        response.put("data", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}