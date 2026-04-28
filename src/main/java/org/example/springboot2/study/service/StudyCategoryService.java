package org.example.springboot2.study.service;

import org.example.springboot2.study.entity.StudyCategory;
import org.example.springboot2.study.repository.StudyCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyCategoryService {

    @Autowired
    private StudyCategoryRepository categoryRepository;

    /**
     * 获取分类列表
     * @param parentId 可选的父分类ID，为null时返回一级分类，否则返回对应父分类下的子分类
     */
    public List<StudyCategory> getCategories(Long parentId) {
        if (parentId != null) {
            return categoryRepository.findByParentId(parentId);
        } else {
            return categoryRepository.findByParentIdIsNull();
        }
    }
}