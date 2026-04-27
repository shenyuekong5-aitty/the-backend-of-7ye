package org.example.springboot2.nickname.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sys_nickname")
public class Nickname {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 无参构造器（JPA 要求）
    public Nickname() {
    }

    // 便捷构造器
    public Nickname(String name) {
        this.name = name;
    }

    // getter / setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}