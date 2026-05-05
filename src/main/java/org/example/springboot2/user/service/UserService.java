package org.example.springboot2.user.service;

import org.example.springboot2.permission.service.PermissionService;
import org.example.springboot2.role.entity.Role;
import org.example.springboot2.role.service.RoleService;
import org.example.springboot2.sms.service.SmsService;
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
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SmsService smsService;

    // 不再注入 QrLoginService

    /**
     * 为用户生成新的 token 并设置过期时间（30天）
     */
    private String generateAndSetToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        user.setTokenExpireTime(LocalDateTime.now().plusDays(30));
        userRepository.save(user);
        return token;
    }

    /**
     * 登录认证（支持用户名或手机号）
     */
    public User login(String account, String rawPassword) {
        User user;
        if (account.matches("^1[3-9]\\d{9}$")) {
            user = userRepository.findByPhone(account);
        } else {
            user = userRepository.findByUsername(account);
        }
        if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            // ✅ 检查是否已注销
            if ("DELETED".equals(user.getStatus())) {
                throw new RuntimeException("该账号已注销，无法登录");
            }
            generateAndSetToken(user);
            return user;
        }
        throw new RuntimeException("账号或密码不正确");
    }

    /**
     * 根据 token 获取用户（无临时token逻辑）
     */
    public User getUserByToken(String token) {
        User user = userRepository.findByToken(token);
        if (user != null) {
            if ("DELETED".equals(user.getStatus())) {
                return null;   // 已注销用户返回 null，前端会提示未登录
            }
            if (user.getTokenExpireTime() != null
                    && user.getTokenExpireTime().isBefore(LocalDateTime.now())) {
                return null;
            }
            return user;
        }
        return null;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

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

    @Transactional
    public boolean logout(String token) {
        User user = userRepository.findByToken(token);
        if (user == null) return false;
        user.setToken(null);
        user.setTokenExpireTime(null);
        userRepository.save(user);
        return true;
    }

    // 安全检测方法
    public Map<String, Object> performSecurityCheck(String token) {
        User user = getUserByToken(token);
        if (user == null) throw new RuntimeException("用户不存在或 Token 无效");

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

        // 2. 权限架构检测
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

        // 3. 菜单合规检测
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void updateUserRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Role role = roleService.getRoleById(roleId);
        if (role == null) throw new RuntimeException("角色不存在");
        user.setRoleId(roleId);
        user.setRole(role.getName());
        userRepository.save(user);
    }

    public User getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        // 如果是已注销用户，清除敏感字段（在实际返回给前端时处理，这里先返回原始对象，由 Controller 决定）
        return user;
    }

    public long countUsersByRoleId(Long roleId) {
        return userRepository.countByRoleId(roleId);
    }

    /**
     * 检查手机号是否被活跃（非注销）用户注册
     */
    public boolean existsByActivePhone(String phone) {
        return userRepository.existsByPhoneAndStatusNot(phone, "DELETED");
    }

    @Transactional
    public User registerByPhone(String username, String phone, String code, String password, String nickname, String avatar) {
        if (!smsService.verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 1. 检查手机号是否被活跃用户占用（已注销的不算）
        if (userRepository.existsByPhoneAndStatusNot(phone, "DELETED")) {
            throw new RuntimeException("手机号已注册");
        }

        // 2. 检查用户名是否被活跃用户占用（已注销的不算）
        if (userRepository.existsByUsernameAndStatusNot(username, "DELETED")) {
            throw new RuntimeException("账号已存在，请更换");
        }

        // 3. 尝试寻找已注销的同名用户或同手机号用户，准备复用
        User existingUser = userRepository.findByUsernameAndStatus(username, "DELETED");
        if (existingUser == null) {
            existingUser = userRepository.findByPhoneAndStatus(phone, "DELETED");
        }

        User user;
        if (existingUser != null) {
            // 复用已注销的用户记录，更新所有字段
            user = existingUser;
            user.setStatus("ACTIVE");
            user.setDeletedAt(null);
        } else {
            // 全新的用户
            user = new User();
            user.setRole("common");
            user.setRoleId(3L);
        }

        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null && !nickname.isBlank() ? nickname : username);
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }

        generateAndSetToken(user);
        return user;
    }
    @Transactional
    public void resetPassword(String phone, String code, String newPassword) {
        if (!smsService.verifyCode(phone, code)) throw new RuntimeException("验证码错误或已过期");
        User user = userRepository.findByPhone(phone);
        if (user == null) throw new RuntimeException("手机号未注册");
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    //注销账号
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus("DELETED");
        user.setToken(null);                     // 清除 token，立即退出
        user.setTokenExpireTime(null);           // 清除过期时间
        user.setDeletedAt(LocalDateTime.now());  // 记录注销时间
        userRepository.save(user);
    }

    //更新头像
    public void updateUser(User user) {
        userRepository.save(user);
    }

    //修改资料
    @Transactional
    public User updateProfile(Long userId, String nickname, String avatar, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 昵称
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname);
        }

        // 头像（Base64）
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }

        // 手机号：需检查是否被其他活跃用户占用
        if (phone != null && !phone.isBlank() && !phone.equals(user.getPhone())) {
            if (userRepository.existsByPhoneAndStatusNot(phone, "DELETED")) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
            user.setPhone(phone);
        }

        return userRepository.save(user);
    }
}