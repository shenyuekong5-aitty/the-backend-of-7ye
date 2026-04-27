package org.example.springboot2.anime.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sys_anime")
public class Anime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "cover_img", columnDefinition = "TEXT")
    private String coverImg;

    private String author;

    @Column(columnDefinition = "TEXT")
    private String brief;

    public Anime() {
    }

    // getter / setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCoverImg() { return coverImg; }
    public void setCoverImg(String coverImg) { this.coverImg = coverImg; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getBrief() { return brief; }
    public void setBrief(String brief) { this.brief = brief; }
}