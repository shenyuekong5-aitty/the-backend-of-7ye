package org.example.springboot2.friend.repository;

import org.example.springboot2.friend.entity.FriendMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendMemoryRepository extends JpaRepository<FriendMemory, Long> {

    // 查询我和指定好友之间的专属回忆（双向匹配）
    @Query("SELECT m FROM FriendMemory m WHERE " +
           "(m.userId = :myId AND m.friendId = :friendId) " +
           "OR (m.userId = :friendId AND m.friendId = :myId) " +
           "ORDER BY m.createTime DESC")
    List<FriendMemory> findMemoriesBetween(@Param("myId") Long myId,
                                           @Param("friendId") Long friendId);
}