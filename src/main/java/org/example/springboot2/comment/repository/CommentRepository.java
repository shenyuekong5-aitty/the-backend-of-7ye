package org.example.springboot2.comment.repository;

import org.example.springboot2.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTargetTypeIsNullAndParentIdIsNullOrderByCreateTimeDesc(Pageable pageable);

    Page<Comment> findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreateTimeDesc(
            String targetType, Long targetId, Pageable pageable);

    List<Comment> findByParentIdOrderByCreateTimeAsc(Long parentId);

    int countByParentId(Long parentId);

    // ✅ 新增：增加点赞数（供 LikeService 调用）
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    // ✅ 新增：减少点赞数
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id AND c.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);
}