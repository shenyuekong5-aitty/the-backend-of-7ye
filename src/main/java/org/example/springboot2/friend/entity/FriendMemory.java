package org.example.springboot2.friend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_friend_memory")
public class FriendMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String photo;   // 存 Base64 或图片 URL

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "memory_time")
    private String memoryTime;   // 如 "2024 夏天"

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    // 无参构造
    public FriendMemory() {}

    // getters & setters (省略，请自行生成或用 Lombok @Data)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFriendId() { return friendId; }
    public void setFriendId(Long friendId) { this.friendId = friendId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMemoryTime() { return memoryTime; }
    public void setMemoryTime(String memoryTime) { this.memoryTime = memoryTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}