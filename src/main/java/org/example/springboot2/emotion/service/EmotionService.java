package org.example.springboot2.emotion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot2.emotion.entity.Emotion;
import org.example.springboot2.emotion.repository.EmotionRepository;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmotionService {

    @Autowired
    private EmotionRepository emotionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;       // Spring Boot 默认有 ObjectMapper，可直接注入

    /**
     * 获取所有情绪列表（按时间倒序），并组装昵称、点赞数
     */
    public List<Map<String, Object>> getAllEmotionsWithDetails() {
        List<Emotion> emotions = emotionRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        Set<Long> userIds = emotions.stream()
                .map(Emotion::getUserid)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userService.getUserMapByIds(userIds);

        return emotions.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("userid", e.getUserid());

            User user = userMap.get(e.getUserid());
            map.put("author", user != null ? user.getNickname() : "未知用户");
            map.put("avatar", user != null ? user.getAvatar() : "");   // 新增头像字段

            map.put("content", e.getContent());
            map.put("time", e.getTime());

            map.put("remarks", e.getRemarksList() != null ? e.getRemarksList() : new ArrayList<>());
            return map;
        }).collect(Collectors.toList());
    }
    /**
     * 根据 ID 获取原始 Emotion 实体（用于内部权限判断）
     */
    public Emotion getById(Long id) {
        return emotionRepository.findById(id).orElse(null);
    }

    /**
     * 新增情绪
     */
    @Transactional
    public Emotion addEmotion(Long userId, String content, String time) {
        Emotion e = new Emotion();
        e.setUserid(userId);
        e.setContent(content);
        e.setTime(time);
        e.setRemarks("[]");      // 初始化为空 JSON 数组
        return emotionRepository.save(e);
    }

    /**
     * 修改情绪内容（仅作者/管理员可调用，需在 Controller 鉴权）
     */
    @Transactional
    public Emotion updateContent(Long emotionId, String newContent) {
        Emotion e = emotionRepository.findById(emotionId)
                .orElseThrow(() -> new RuntimeException("情绪不存在"));
        e.setContent(newContent);
        return emotionRepository.save(e);
    }

    /**
     * 删除情绪（仅作者/管理员可调用）
     */
    @Transactional
    public void deleteEmotion(Long emotionId) {
        if (!emotionRepository.existsById(emotionId)) {
            throw new RuntimeException("情绪不存在");
        }
        emotionRepository.deleteById(emotionId);
    }

    /**
     * 追加回复（remarks 数组追加一条字符串）
     */
    @Transactional
    public Emotion addReply(Long emotionId, String replyContent) {
        Emotion e = emotionRepository.findById(emotionId)
                .orElseThrow(() -> new RuntimeException("情绪不存在"));

        List<String> replyList;
        try {
            replyList = objectMapper.readValue(e.getRemarks(), new TypeReference<List<String>>() {});
        } catch (JsonProcessingException ex) {
            replyList = new ArrayList<>();
        }

        replyList.add(replyContent);

        try {
            e.setRemarks(objectMapper.writeValueAsString(replyList));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("回复数据序列化失败");
        }
        return emotionRepository.save(e);
    }
}