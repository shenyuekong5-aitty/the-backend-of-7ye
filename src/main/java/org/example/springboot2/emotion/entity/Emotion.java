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
    private Long id;

    @Column(name = "userid")
    private Long userid;

    private String author;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String time;

    @Column(name = "\"like\"")
    @JsonProperty("like")   // 前端使用 "like"，序列化时映射为 like
    private Integer likeCount;

    @JsonIgnore   // 忽略原字符串字段，不返回给前端
    @Column(columnDefinition = "TEXT")
    private String remarks;

    // 新增：解析后的 remarks 数组，返回给前端
    @Transient
    @JsonProperty("remarks")
    private List<String> remarksList;

    public Emotion() {
    }

    // 在加载后自动解析（可选）
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

    // getter / setter（保留原有字段的，新增 remarksList 的 getter/setter）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserid() { return userid; }
    public void setUserid(Long userid) { this.userid = userid; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public List<String> getRemarksList() { return remarksList; }
    public void setRemarksList(List<String> remarksList) { this.remarksList = remarksList; }
}