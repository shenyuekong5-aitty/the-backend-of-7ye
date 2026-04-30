package org.example.springboot2.user.service;

import org.example.springboot2.permission.service.PermissionService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionService permissionService;   // ✅ 新增

    // 登录认证
    public User login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }

    // 根据 token 获取用户
    public User getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    // 检查用户名是否存在
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // 修改密码
    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) return false;
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordUpdateTime(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    /**
     * 用户退出登录，清除数据库中的 token
     */
    @Transactional
    public boolean logout(String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            return false;
        }
        user.setToken(null);
        userRepository.save(user);
        return true;
    }

    // 安全检测
    public Map<String, Object> performSecurityCheck(String token) {
        User user = getUserByToken(token);
        if (user == null) {
            throw new RuntimeException("用户不存在或 Token 无效");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        int totalScore = 100;

        // 1. 密码安全检测
        Map<String, Object> pwdItem = new HashMap<>();
        pwdItem.put("id", "pwd");
        pwdItem.put("label", "密码安全");
        LocalDateTime pwdUpdateTime = user.getPasswordUpdateTime();
        long daysSinceUpdate = pwdUpdateTime == null ? 999
                : ChronoUnit.DAYS.between(pwdUpdateTime, LocalDateTime.now());
        if (daysSinceUpdate > 90) {
            pwdItem.put("result", "超过90天未更换");
            pwdItem.put("status", "warning");
            totalScore -= 15;
        } else {
            pwdItem.put("result", "正常");
            pwdItem.put("status", "success");
        }
        items.add(pwdItem);

        // 2. 权限架构检测（基于角色字符串判断）
        Map<String, Object> roleItem = new HashMap<>();
        roleItem.put("id", "role");
        roleItem.put("label", "权限架构");
        if ("admin".equals(user.getRole())) {
            roleItem.put("result", "超级管理员 (至尊权限)");
        } else if ("friend".equals(user.getRole())) {
            roleItem.put("result", "朋友 (高权限)");
        } else {
            roleItem.put("result", "普通用户");
        }
        roleItem.put("status", "success");
        items.add(roleItem);

        // 3. 菜单合规检测（改为动态获取权限）
        Map<String, Object> routeItem = new HashMap<>();
        routeItem.put("id", "route");
        routeItem.put("label", "菜单合规");
        int routeCount = 0;
        if (user.getRoleId() != null) {
            List<String> permissions = permissionService.getPermissionsByRoleId(user.getRoleId());
            routeCount = permissions.size();
        }
        if (routeCount > 10) {
            routeItem.put("result", "功能完整");
            routeItem.put("status", "success");
        } else {
            routeItem.put("result", "功能受限");
            routeItem.put("status", "warning");
            totalScore -= 10;
        }
        items.add(routeItem);

        // 4. 资料完整度
        Map<String, Object> infoItem = new HashMap<>();
        infoItem.put("id", "info");
        infoItem.put("label", "资料完整度");
        boolean hasAvatar = user.getAvatar() != null && !user.getAvatar().isEmpty();
        boolean hasDesc = user.getDesc() != null && !user.getDesc().isEmpty();
        if (hasAvatar && hasDesc) {
            infoItem.put("result", "已完善");
            infoItem.put("status", "success");
        } else if (hasAvatar || hasDesc) {
            infoItem.put("result", "部分完善");
            infoItem.put("status", "warning");
            totalScore -= 5;
        } else {
            infoItem.put("result", "未完善");
            infoItem.put("status", "warning");
            totalScore -= 10;
        }
        items.add(infoItem);

        result.put("score", Math.max(totalScore, 0));
        result.put("items", items);
        result.put("message", totalScore >= 90 ? "账号整体状态良好" : "存在安全隐患，建议优化");
        return result;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}