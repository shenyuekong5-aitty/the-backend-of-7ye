package org.example.springboot2.study.repository;

import org.example.springboot2.study.entity.StudyCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyCategoryRepository extends JpaRepository<StudyCategory, Long> {

    /** 查询某一级分类下的所有子分类 */
    List<StudyCategory> findByParentId(Long parentId);

    /** 查询所有一级分类（parentId IS NULL） */
    List<StudyCategory> findByParentIdIsNull();
}