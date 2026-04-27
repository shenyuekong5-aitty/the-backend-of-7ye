package org.example.springboot2.interaction.view.repository;

import org.example.springboot2.interaction.view.entity.UserView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserViewRepository extends JpaRepository<UserView, Long> {
    // 可扩展统计方法
}