package org.example.springboot2.recommendation.repository;

import org.example.springboot2.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByStatusOrderByCreateTimeDesc(String status);
    List<Recommendation> findByTypeAndStatus(String type, String status);
    List<Recommendation> findAllByOrderByCreateTimeDesc();
    List<Recommendation> findByProposerIdOrderByCreateTimeDesc(Long proposerId);
}