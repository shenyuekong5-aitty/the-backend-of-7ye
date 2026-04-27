package org.example.springboot2.favorite.repository;

import org.example.springboot2.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // 判断是否已收藏
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    // 取消收藏
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    // 获取用户收藏列表（分页）
    Page<Favorite> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    // 获取用户对某个类型的所有收藏
    List<Favorite> findByUserIdAndTargetType(Long userId, String targetType);

    // 获取某个目标的总收藏数
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}