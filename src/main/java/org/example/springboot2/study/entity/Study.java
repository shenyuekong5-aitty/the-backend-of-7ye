package org.example.springboot2.study.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 学习资源实体类
 * 对应数据库表 sys_study，存储学习分享的基本信息和统计数据
 */
@Entity
@Table(name = "sys_study")  // 映射到统一命名规范后的 sys_study 表
public class Study {

    /** 主键，自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 学习资源标题，不可为空，最大200字符 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 内容描述，TEXT 长文本 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 优点分析，TEXT 长文本 */
    @Column(columnDefinition = "TEXT")
    private String advantage;

    /** 缺点分析，TEXT 长文本 */
    @Column(columnDefinition = "TEXT")
    private String disadvantage;

    /** 作者（发布者）的用户ID，不可为空，关联 sys_user 表 */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /** 作者用户名，冗余字段，用于快速展示作者名，避免 JOIN 查询 */
    @Column(name = "author_name", length = 50)
    private String authorName;

    /** 浏览量（冗余计数字段，由浏览服务维护） */
    @Column(name = "view_count")
    private Integer viewCount = 0;

    /** 点赞数（冗余计数字段，由点赞服务维护） */
    @Column(name = "like_count")
    private Integer likeCount = 0;

    /** 收藏数（冗余计数字段，由收藏服务维护） */
    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    /** 创建时间，插入时自动设置 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 最后更新时间，插入和更新时自动设置 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 插入操作前的回调，自动填充创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    /**
     * 更新操作前的回调，自动更新最后修改时间
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // ===================== Getter / Setter =====================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAdvantage() { return advantage; }
    public void setAdvantage(String advantage) { this.advantage = advantage; }
    public String getDisadvantage() { return disadvantage; }
    public void setDisadvantage(String disadvantage) { this.disadvantage = disadvantage; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}