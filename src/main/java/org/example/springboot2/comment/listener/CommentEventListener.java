package org.example.springboot2.comment.listener;

import org.example.springboot2.user.event.UserProfileUpdatedEvent;
import org.example.springboot2.user.service.UserDataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 监听用户资料更新事件，通过 UserDataSyncService 批量同步评论表中的冗余昵称。
 */
@Component
public class CommentEventListener {

    @Autowired
    private UserDataSyncService dataSyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserProfileUpdated(UserProfileUpdatedEvent event) {
        dataSyncService.syncUserProfileUpdate(
                event.getUserId(),
                event.getNewNickname(),
                event.getNewAvatar());
    }
}