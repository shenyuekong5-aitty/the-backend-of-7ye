package org.example.springboot2.task;

import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CleanDeletedUsersTask {

    @Autowired
    private UserRepository userRepository;

    /**
     * 每月1日凌晨2点清理注销超过1年的用户
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void cleanLongDeletedUsers() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<User> users = userRepository.findByStatusAndDeletedAtBefore("DELETED", oneYearAgo);
        for (User user : users) {
            userRepository.delete(user);
        }
    }
}