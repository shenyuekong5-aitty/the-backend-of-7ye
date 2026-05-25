package org.example.springboot2.friend.repository;

import org.example.springboot2.friend.entity.FriendMemory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendMemoryRepository extends JpaRepository<FriendMemory, Long> {

    // 分页查询专属回忆（双向匹配）
    @Query(value = "SELECT m FROM FriendMemory m WHERE " +
            "(m.userId = :myId AND m.friendId = :friendId) " +
            "OR (m.userId = :friendId AND m.friendId = :myId) " +
            "ORDER BY m.createTime DESC",
            countQuery = "SELECT count(m) FROM FriendMemory m WHERE " +
                    "(m.userId = :myId AND m.friendId = :friendId) " +
                    "OR (m.userId = :friendId AND m.friendId = :myId)")
    Page<FriendMemory> findMemoriesBetween(@Param("myId") Long myId,
                                           @Param("friendId") Long friendId,
                                           Pageable pageable);
}