package org.example.springboot2.tag.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sys_tag")
public class Tag {

    @Id
    private Long id;   // Mock 数据中指定了 id，不使用自增

    @Column(nullable = false)
    private String content;

    public Tag() {
    }

    // getter / setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}