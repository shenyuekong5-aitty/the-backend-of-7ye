package org.example.springboot2.study.service;

import org.example.springboot2.study.entity.Study;
import org.example.springboot2.study.entity.StudyCategory;
import org.example.springboot2.study.repository.StudyCategoryRepository;
import org.example.springboot2.study.repository.StudyRepository;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudyService {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyCategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    /**
     * 分页获取学习列表，可按分类筛选
     * @param pageNo   页码
     * @param pageSize 每页条数
     * @param categoryId 分类ID，为null时查询全部
     */
    public Map<String, Object> getStudyList(int pageNo, int pageSize, Long categoryId, Long parentCategoryId) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Study> page;

        if (categoryId != null) {
            page = studyRepository.findByCategoryIdOrderByCreateTimeDesc(categoryId, pageable);
        } else if (parentCategoryId != null) {
            List<StudyCategory> subCategories = categoryRepository.findByParentId(parentCategoryId);
            List<Long> subIds = subCategories.stream()
                    .map(StudyCategory::getId)
                    .collect(Collectors.toList());
            if (subIds.isEmpty()) {
                page = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else {
                page = studyRepository.findByCategoryIdInOrderByCreateTimeDesc(subIds, pageable);
            }
        } else {
            page = studyRepository.findAllByOrderByCreateTimeDesc(pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    public Study getStudyById(Long id) {
        return studyRepository.findById(id).orElse(null);
    }

    @Transactional
    public Study addStudy(String token, Study study) {
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        study.setId(null);
        study.setAuthorId(user.getId());
        study.setAuthorName(user.getUsername());
        study.setViewCount(0);
        study.setLikeCount(0);
        study.setFavoriteCount(0);
        // categoryId 由前端传入，直接保存
        return studyRepository.save(study);
    }

    @Transactional
    public Study updateStudy(String token, Study study) {
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        Study existing = studyRepository.findById(study.getId())
                .orElseThrow(() -> new RuntimeException("学习条目不存在"));

        boolean isAdmin = user.getRoles() != null && user.getRoles().contains("admin");
        boolean isAuthor = existing.getAuthorId().equals(user.getId());
        if (!isAdmin && !isAuthor) {
            throw new RuntimeException("无权修改他人创建的学习条目");
        }

        existing.setTitle(study.getTitle());
        existing.setDescription(study.getDescription());
        existing.setAdvantage(study.getAdvantage());
        existing.setDisadvantage(study.getDisadvantage());
        existing.setCategoryId(study.getCategoryId());  // 允许修改分类
        return studyRepository.save(existing);
    }

    @Transactional
    public void deleteStudy(String token, Long id) {
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        Study existing = studyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学习条目不存在"));

        boolean isAdmin = user.getRoles() != null && user.getRoles().contains("admin");
        boolean isAuthor = existing.getAuthorId().equals(user.getId());
        if (!isAdmin && !isAuthor) {
            throw new RuntimeException("无权删除他人创建的学习条目");
        }
        studyRepository.delete(existing);
    }

    @Transactional
    public void viewStudy(Long id) {
        studyRepository.incrementViewCount(id);
    }
}