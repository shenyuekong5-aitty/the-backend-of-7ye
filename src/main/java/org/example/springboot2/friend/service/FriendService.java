package org.example.springboot2.friend.service;

import lombok.RequiredArgsConstructor;
import org.example.springboot2.friend.entity.Friend;
import org.example.springboot2.friend.entity.FriendMemory;
import org.example.springboot2.friend.repository.FriendMemoryRepository;
import org.example.springboot2.friend.repository.FriendRepository;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final FriendMemoryRepository memoryRepository;
    private final UserService userService;

    // ========== 好友管理 ==========

    /** 获取我的好友列表（返回好友基本信息） */
    public List<Map<String, Object>> getFriendList(Long myUserId) {
        List<Friend> friends = friendRepository.findByUserIdOrderByCreateTimeDesc(myUserId);
        return friends.stream().map(f -> {
            User friendUser = userService.getUserById(f.getFriendUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("friendId", f.getFriendUserId());
            map.put("username", friendUser != null ? friendUser.getUsername() : "未知");
            map.put("nickname", friendUser != null ? friendUser.getNickname() : "未知");
            map.put("avatar", friendUser != null ? friendUser.getAvatar() : "");
            map.put("createTime", f.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }

    // 获取所有角色为 friend 的用户列表（供管理员切换）
    public List<Map<String, Object>> getFriendUsers() {
        List<User> friendUsers = userService.getUsersByRole("friend");
        return friendUsers.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getId());
            String nickname = u.getNickname();
            if (nickname == null || nickname.isBlank()) {
                nickname = u.getUsername();
            }
            map.put("nickname", u.getNickname());
            map.put("avatar", u.getAvatar());
            return map;
        }).collect(Collectors.toList());
    }

    /** 添加好友（双向） */
    @Transactional
    public void addFriend(Long myUserId, Long friendUserId) {
        if (myUserId.equals(friendUserId)) throw new RuntimeException("不能添加自己");
        if (friendRepository.existsByUserIdAndFriendUserId(myUserId, friendUserId))
            throw new RuntimeException("已经是好友了");

        friendRepository.save(new Friend(myUserId, friendUserId));
        friendRepository.save(new Friend(friendUserId, myUserId));
    }

    /** 删除好友（双向删除） */
    @Transactional
    public void removeFriend(Long myUserId, Long friendUserId) {
        friendRepository.deleteByUserIdAndFriendUserId(myUserId, friendUserId);
        friendRepository.deleteByUserIdAndFriendUserId(friendUserId, myUserId);
    }

    // ========== 回忆管理 ==========

    /** 获取我和某好友之间的专属回忆（分页） */
    public Page<FriendMemory> getMemoriesBetween(Long myUserId, Long friendUserId, Pageable pageable) {
        return memoryRepository.findMemoriesBetween(myUserId, friendUserId, pageable);
    }

    /** 添加一条回忆 */
    @Transactional
    public FriendMemory addMemory(Long userId, Long friendId, String title,
                                  String photo, String description, String memoryTime) {
        if (!friendRepository.existsByUserIdAndFriendUserId(userId, friendId)) {
            friendRepository.save(new Friend(userId, friendId));
            friendRepository.save(new Friend(friendId, userId));
        }

        if (!friendRepository.existsByUserIdAndFriendUserId(userId, friendId))
            throw new RuntimeException("你们还不是好友");

        FriendMemory memory = new FriendMemory();
        memory.setUserId(userId);
        memory.setFriendId(friendId);
        memory.setTitle(title);
        memory.setPhoto(photo);
        memory.setDescription(description);
        memory.setMemoryTime(memoryTime);
        return memoryRepository.save(memory);
    }

    /** 删除回忆（仅作者或管理员可操作，权限由 Controller 校验） */
    @Transactional
    public void deleteMemory(Long memoryId) {
        memoryRepository.deleteById(memoryId);
    }
}