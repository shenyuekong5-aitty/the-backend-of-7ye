package org.example.springboot2.user.controller;

import org.example.springboot2.permission.service.PermissionService;
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
public class UserController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    // 登录
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginParams) {
        String username = loginParams.get("username");
        String password = loginParams.get("password");
        User user = userService.login(username, password);
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("code", 200);
            response.put("data", Map.of("token", user.getToken()));
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 201);
            response.put("data", Map.of("message", "账号或密码不正确"));
            return ResponseEntity.ok(response);
        }
    }

    // 获取当前用户信息
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("token") String token) {
        User user = userService.getUserByToken(token);
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            // 构建安全的用户信息，不直接返回整个 user 实体
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", user.getUsername());
            userMap.put("avatar", user.getAvatar());
            userMap.put("role", user.getRole());          // 单个角色字符串
            userMap.put("userid", user.getId());
            userMap.put("desc", user.getDesc());
            userMap.put("createTime", user.getCreateTime());

            // 关键：动态查询权限
            List<String> permissions = permissionService.getPermissionsByRoleId(user.getRoleId());
            userMap.put("routes", permissions);           // 保持字段名一致

            response.put("code", 200);
            response.put("data", Map.of("user", userMap));
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 201);
            response.put("data", Map.of("message", "获取用户信息失败"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // 检查用户名是否存在
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("exists", exists));
        response.put("message", exists ? "用户名已存在" : "用户名可用");
        return ResponseEntity.ok(response);
    }

    // 修改密码
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> params) {
        User currentUser = userService.getUserByToken(token);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "未认证或 Token 无效");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        boolean success = userService.changePassword(currentUser.getUsername(), oldPassword, newPassword);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("code", 200);
            response.put("data", Map.of("message", "密码修改成功，请重新登录"));
        } else {
            response.put("code", 201);
            response.put("data", Map.of("message", "旧密码错误或用户不存在"));
        }
        return ResponseEntity.ok(response);
    }

    // 退出登录
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        // 从请求头中提取 token（假设格式为 "Bearer <token>"）
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            response.put("code", 401);
            response.put("message", "缺少认证 token");
            response.put("data", null);
            return ResponseEntity.ok(response);
        }

        boolean success = userService.logout(token);
        if (success) {
            response.put("code", 200);
            response.put("message", "登出成功");
            response.put("data", null);
        } else {
            response.put("code", 401);
            response.put("message", "token 无效或用户不存在");
            response.put("data", null);
        }
        return ResponseEntity.ok(response);
    }

    // 安全检测
    @GetMapping("/security-check")
    public ResponseEntity<Map<String, Object>> securityCheck(@RequestHeader("token") String token) {
        try {
            Map<String, Object> data = userService.performSecurityCheck(token);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // 根据ID获取用户公开信息
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            response.put("code", 404);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }
        Map<String, Object> publicInfo = new HashMap<>();
        publicInfo.put("id", user.getId());
        publicInfo.put("username", user.getUsername());
        publicInfo.put("avatar", user.getAvatar());
        publicInfo.put("desc", user.getDesc());
        publicInfo.put("role", user.getRole());
        publicInfo.put("createTime", user.getCreateTime()); // 新增
        // 不返回密码、token等敏感字段

        response.put("code", 200);
        response.put("data", publicInfo);
        return ResponseEntity.ok(response);
    }
}