package org.example.springboot2.study.repository;

import org.example.springboot2.study.entity.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {

    /** 查询所有，按创建时间倒序 */
    Page<Study> findAllByOrderByCreateTimeDesc(Pageable pageable);

    /** 按分类ID查询，按创建时间倒序 */
    Page<Study> findByCategoryIdOrderByCreateTimeDesc(Long categoryId, Pageable pageable);

    // 按多个 categoryId 查询
    Page<Study> findByCategoryIdInOrderByCreateTimeDesc(List<Long> categoryIds, Pageable pageable);

    @Modifying
    @Query("UPDATE Study s SET s.likeCount = s.likeCount + 1 WHERE s.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Study s SET s.likeCount = s.likeCount - 1 WHERE s.id = :id AND s.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Study s SET s.favoriteCount = s.favoriteCount + 1 WHERE s.id = :id")
    void incrementFavoriteCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Study s SET s.favoriteCount = s.favoriteCount - 1 WHERE s.id = :id AND s.favoriteCount > 0")
    void decrementFavoriteCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Study s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    void incrementViewCount(@Param("id") Long id);
}