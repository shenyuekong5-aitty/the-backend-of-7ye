package org.example.springboot2.user.event;

import lombok.Getter;

/**
 * 用户资料更新事件，携带需要同步的 userId、新昵称、新头像
 */
@Getter
public class UserProfileUpdatedEvent {
    private final Long userId;
    private final String newNickname;
    private final String newAvatar;

    public UserProfileUpdatedEvent(Long userId, String newNickname, String newAvatar) {
        this.userId = userId;
        this.newNickname = newNickname;
        this.newAvatar = newAvatar;
    }
}