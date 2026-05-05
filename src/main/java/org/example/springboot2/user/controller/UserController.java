package org.example.springboot2.user.controller;

import org.example.springboot2.permission.service.PermissionService;
import org.example.springboot2.role.service.RoleService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    // 登录
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginParams) {
        String account = loginParams.get("username");
        String password = loginParams.get("password");
        try {
            User user = userService.login(account, password);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", Map.of("token", user.getToken()));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("data", Map.of("message", e.getMessage()));
            return ResponseEntity.ok(response);
        }
    }
    // 获取当前用户信息
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("token") String token) {
        User user = userService.getUserByToken(token);
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", user.getUsername());
            userMap.put("avatar", user.getAvatar());
            userMap.put("nickname", user.getNickname());
            userMap.put("role", user.getRole());
            userMap.put("userid", user.getId());
            userMap.put("desc", user.getDesc());
            userMap.put("createTime", user.getCreateTime());

            // ✅ 在这里添加 isDeleted 标识
            if ("DELETED".equals(user.getStatus())) {
                userMap.put("isDeleted", true);
            } else {
                userMap.put("isDeleted", false);
            }

            List<String> permissions = permissionService.getPermissionsByRoleId(user.getRoleId());
            userMap.put("routes", permissions);

            response.put("code", 200);
            response.put("data", Map.of("user", userMap));
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 201);
            response.put("data", Map.of("message", "获取用户信息失败，未登录账号或账号已注销"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    // 获取所有用户（用于角色分配表格）
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> userList() {
        // 保持原样即可，如需展示昵称可自行添加
        List<User> users = userService.getAllUsers();
        List<Map<String, Object>> list = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("avatar", user.getAvatar());
            map.put("role", user.getRole());
            map.put("roleId", user.getRoleId());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", list);
        return ResponseEntity.ok(response);
    }

    // 修改用户角色
    @PutMapping("/{userId}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, Long> body) {
        Long roleId = body.get("roleId");
        userService.updateUserRole(userId, roleId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "角色更新成功");
        return ResponseEntity.ok(response);
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
        // 通用字段
        publicInfo.put("id", user.getId());
        publicInfo.put("desc", user.getDesc());
        publicInfo.put("role", user.getRole());
        publicInfo.put("createTime", user.getCreateTime());

        // 根据状态设置差异字段
        if ("DELETED".equals(user.getStatus())) {
            publicInfo.put("username", "已注销用户");
            publicInfo.put("nickname", "已注销用户");
            publicInfo.put("avatar", "/default-avatar.png");
            publicInfo.put("isDeleted", true);
        } else {
            publicInfo.put("username", user.getUsername());
            publicInfo.put("nickname", user.getNickname());
            publicInfo.put("avatar", user.getAvatar());
            publicInfo.put("isDeleted", false);
        }

        response.put("code", 200);
        response.put("data", publicInfo);
        return ResponseEntity.ok(response);
    }
    // 手机号注册（接收账号、密码、昵称）
    @PostMapping("/register-by-phone")
    public ResponseEntity<Map<String, Object>> registerByPhone(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String phone = body.get("phone");
        String code = body.get("code");
        String password = body.get("password");
        String nickname = body.get("nickname");

        if (username == null || phone == null || code == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "参数不完整"));
        }
        try {
            User user = userService.registerByPhone(username, phone, code, password, nickname);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 200);
            resp.put("data", Map.of("token", user.getToken(), "userId", user.getId()));
            resp.put("message", "注册成功");
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        }
    }
    //根据手机号验证码设置密码
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        String newPassword = body.get("newPassword");

        if (phone == null || code == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "参数不完整"));
        }
        try {
            userService.resetPassword(phone, code, newPassword);
            return ResponseEntity.ok(Map.of("code", 200, "message", "密码重置成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        }
    }
    //检查手机号是否已经注册
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("exists", exists));
        response.put("message", exists ? "手机号已注册" : "手机号可用");
        return ResponseEntity.ok(response);
    }
    //注销手机号
    @PostMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> deactivate(@RequestHeader("token") String token) {
        User currentUser = userService.getUserByToken(token);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "code", 401,
                    "message", "未登录"
            ));
        }
        userService.deactivateUser(currentUser.getId());
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "账号已注销"
        ));
    }
}