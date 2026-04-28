package org.example.springboot2.study.repository;

import org.example.springboot2.study.entity.StudyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudyCategoryRepository extends JpaRepository<StudyCategory, Long> {
    List<StudyCategory> findByParentId(Long parentId);
    List<StudyCategory> findByParentIdIsNull();
}