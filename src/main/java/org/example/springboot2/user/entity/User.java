package org.example.springboot2.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    private String avatar;

    @Column(name = "`desc`")
    private String desc;

    @Column(name = "password_update_time")
    private LocalDateTime passwordUpdateTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 单个角色字符串 (admin/friend/common)
    private String role;

    // 关联角色表的主键ID（替代原 routes 字段）
    @Column(name = "role_id")
    private Long roleId;


    private String token;

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    // ========== Getter / Setter ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }   // 修正方法名

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getPasswordUpdateTime() { return passwordUpdateTime; }
    public void setPasswordUpdateTime(LocalDateTime passwordUpdateTime) { this.passwordUpdateTime = passwordUpdateTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}