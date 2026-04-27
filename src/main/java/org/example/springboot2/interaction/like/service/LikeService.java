package org.example.springboot2.interaction.like.service;

import org.example.springboot2.comment.repository.CommentRepository;
import org.example.springboot2.interaction.like.entity.UserLike;
import org.example.springboot2.interaction.like.repository.UserLikeRepository;
import org.example.springboot2.study.repository.StudyRepository;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {
    @Autowired private UserLikeRepository likeRepository;
    @Autowired private StudyRepository studyRepository;
    @Autowired private CommentRepository commentRepository;   // ✅ 注入评论 Repository
    @Autowired private UserService userService;

    @Transactional
    public boolean toggleLike(String token, String targetType, Long targetId) {
        User user = userService.getUserByToken(token);
        if (user == null) throw new RuntimeException("请先登录");

        if (likeRepository.existsByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId)) {
            likeRepository.deleteByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId);
            updateTargetLikeCount(targetType, targetId, -1);
            return false;
        } else {
            UserLike like = new UserLike();
            like.setUserId(user.getId());
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            likeRepository.save(like);
            updateTargetLikeCount(targetType, targetId, 1);
            return true;
        }
    }

    public boolean isLiked(Long userId, String targetType, Long targetId) {
        if (userId == null) return false;
        return likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    private void updateTargetLikeCount(String targetType, Long targetId, int delta) {
        switch (targetType) {
            case "study":
                if (delta > 0) studyRepository.incrementLikeCount(targetId);
                else studyRepository.decrementLikeCount(targetId);
                break;
            case "comment":
                if (delta > 0) commentRepository.incrementLikeCount(targetId);
                else commentRepository.decrementLikeCount(targetId);
                break;
            // 未来可扩展其他模块
        }
    }
}