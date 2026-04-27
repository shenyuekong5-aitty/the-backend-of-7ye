package org.example.springboot2.study.service;

import org.example.springboot2.study.entity.Study;
import org.example.springboot2.study.repository.StudyRepository;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class StudyService {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserService userService;

    public Map<String, Object> getStudyList(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Study> page = studyRepository.findAllByOrderByCreateTimeDesc(pageable);

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