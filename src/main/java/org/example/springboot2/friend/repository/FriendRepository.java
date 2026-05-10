package org.example.springboot2.friend.repository;

import org.example.springboot2.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 获取某用户的所有好友 ID
    List<Friend> findByUserIdOrderByCreateTimeDesc(Long userId);

    // 检查是否为好友关系
    boolean existsByUserIdAndFriendUserId(Long userId, Long friendUserId);

    // 删除双向关系时使用
    void deleteByUserIdAndFriendUserId(Long userId, Long friendUserId);
}