package org.example.springboot2.emotion.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_emotion")
public class Emotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userid")
    private Long userid;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String time;

    // 数据库存储为字符串，不直接返回前端
    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String remarks;

    // 返回给前端的数组（数据库存放的是字符串，需要处理）
    @Transient
    @JsonProperty("remarks")
    private List<String> remarksList;

    public Emotion() {}

    @PostLoad
    private void parseRemarks() {
        if (remarks != null && !remarks.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.remarksList = mapper.readValue(remarks, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                this.remarksList = new ArrayList<>();
            }
        } else {
            this.remarksList = new ArrayList<>();
        }
    }

    // ========== Getters & Setters ==========
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserid() {
        return userid;
    }
    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<String> getRemarksList() {
        return remarksList;
    }
    public void setRemarksList(List<String> remarksList) {
        this.remarksList = remarksList;
    }
}