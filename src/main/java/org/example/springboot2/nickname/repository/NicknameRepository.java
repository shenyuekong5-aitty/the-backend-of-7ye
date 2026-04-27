package org.example.springboot2.nickname.repository;

import org.example.springboot2.nickname.entity.Nickname;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NicknameRepository extends JpaRepository<Nickname, Long> {
    // 根据名称模糊查询（忽略大小写）
    List<Nickname> findByNameContainingIgnoreCase(String keyword);

    // 根据名称精确查找（用于唯一性校验）
    boolean existsByName(String name);

    // 根据名称删除
    void deleteByName(String name);
}