package org.example.springboot2.study.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_study_category")
public class StudyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 50, unique = true)
    private String name;          // 分类名称，如 "Frontend"、"Backend"、"ComputerBase"

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    // ========== Getter / Setter ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() {return parentId;}
    public void setParentId(Long parentId) {this.parentId = parentId;}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}