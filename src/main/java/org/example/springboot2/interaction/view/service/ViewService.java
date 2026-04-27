package org.example.springboot2.interaction.view.service;

import org.example.springboot2.interaction.view.entity.UserView;
import org.example.springboot2.interaction.view.repository.UserViewRepository;
import org.example.springboot2.study.repository.StudyRepository;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewService {
    @Autowired private UserViewRepository viewRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private UserService userService;

    /**
     * 增加浏览记录并返回最新的 viewCount
     */
    @Transactional
    public int addView(String token, String targetType, Long targetId) {
        User user = null;
        if (token != null && !token.isEmpty()) {
            user = userService.getUserByToken(token);
        }

        UserView view = new UserView();
        view.setUserId(user != null ? user.getId() : null);
        view.setTargetType(targetType);
        view.setTargetId(targetId);
        viewRepository.save(view);

        return updateTargetViewCount(targetType, targetId);
    }

    private int updateTargetViewCount(String targetType, Long targetId) {
        if ("study".equals(targetType)) {
            studyRepository.incrementViewCount(targetId);
            return studyRepository.findById(targetId)
                    .map(study -> study.getViewCount())
                    .orElse(0);
        }
        // 其他模块可扩展
        return 0;
    }
}