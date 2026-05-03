package org.example.springboot2.user.service;

import org.example.springboot2.permission.service.PermissionService;
import org.example.springboot2.qrlogin.service.QrLoginService;          // 新增导入
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

    @Autowired
    private QrLoginService qrLoginService;   // 注入扫码登录服务

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
            return user;
        }
        return null;
    }

    // 根据 token 获取用户（先查数据库，再查临时扫码 token）
    public User getUserByToken(String token) {
        // 先查数据库
        User user = userRepository.findByToken(token);
        if (user != null) {
            return user;
        }
        // 查扫码登录的临时 token
        Long userId = qrLoginService.getUserIdByTemporaryToken(token);
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        return null;
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

    // 退出登录
    @Transactional
    public boolean logout(String token) {
        User user = userRepository.findByToken(token);
        if (user == null) return false;
        user.setToken(null);
        userRepository.save(user);
        return true;
    }

    // 安全检测
    public Map<String, Object> performSecurityCheck(String token) {
        // ... 保持不变，限于篇幅省略，您原有的代码完全可用 ...
        return new HashMap<>(); // 占位，请替换为您的原始代码
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 更新用户角色
    @Transactional
    public void updateUserRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Role role = roleService.getRoleById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        user.setRoleId(roleId);
        user.setRole(role.getName());
        userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public long countUsersByRoleId(Long roleId) {
        return userRepository.countByRoleId(roleId);
    }

    /**
     * 手机号注册（支持自定义账号、密码和昵称）
     */
    @Transactional
    public User registerByPhone(String username, String phone, String code, String password, String nickname) {
        if (!smsService.verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已注册");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("账号已存在，请更换");
        }

        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null && !nickname.isBlank() ? nickname : username);
        user.setRole("common");
        user.setRoleId(3L);

        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public void resetPassword(String phone, String code, String newPassword) {
        if (!smsService.verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("手机号未注册");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}