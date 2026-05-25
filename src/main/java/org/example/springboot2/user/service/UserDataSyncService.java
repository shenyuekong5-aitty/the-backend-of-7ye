package org.example.springboot2.user.service;

import org.example.springboot2.comment.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处理用户资料变更后的冗余数据同步。
 * 使用独立事务（REQUIRES_NEW）确保批量更新不依赖主事务。
 */
@Service
public class UserDataSyncService {

    @Autowired
    private CommentRepository commentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncUserProfileUpdate(Long userId, String newNickname, String newAvatar) {
        if (newNickname != null) {
            commentRepository.updateNicknameByUserId(userId, newNickname);
        }
        if (newAvatar != null) {
            commentRepository.updateAvatarByUserId(userId, newAvatar);
        }
    }
}