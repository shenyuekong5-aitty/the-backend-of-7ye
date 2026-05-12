package org.example.springboot2.interaction.favorite.service;

import org.example.springboot2.cognize.entity.Cognize;
import org.example.springboot2.cognize.repository.CognizeRepository;
import org.example.springboot2.creed.entity.Creed;
import org.example.springboot2.creed.repository.CreedRepository;
import org.example.springboot2.emotion.entity.Emotion;
import org.example.springboot2.emotion.repository.EmotionRepository;
import org.example.springboot2.favorite.entity.Favorite;
import org.example.springboot2.interaction.favorite.repository.FavoriteRepository;
import org.example.springboot2.quote.entity.Quote;
import org.example.springboot2.quote.repository.QuoteRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private EmotionRepository emotionRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private CreedRepository creedRepository;

    @Autowired
    private CognizeRepository cognizeRepository;

    /**
     * 收藏操作
     */
    @Transactional
    public void addFavorite(String token, String targetType, Long targetId) {
        User user = userService.getUserByToken(token);
        if (user == null) throw new RuntimeException("请先登录");

        if (favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId)) {
            throw new RuntimeException("已经收藏过了");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(user.getId());
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favoriteRepository.save(favorite);

        updateFavoriteCount(targetType, targetId, 1);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(String token, String targetType, Long targetId) {
        User user = userService.getUserByToken(token);
        if (user == null) throw new RuntimeException("请先登录");

        if (!favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId)) {
            throw new RuntimeException("尚未收藏");
        }

        favoriteRepository.deleteByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId);

        updateFavoriteCount(targetType, targetId, -1);
    }

    /**
     * 切换收藏状态
     */
    @Transactional
    public boolean toggleFavorite(String token, String targetType, Long targetId) {
        User user = userService.getUserByToken(token);
        if (user == null) throw new RuntimeException("请先登录");

        boolean exists = favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId);
        if (exists) {
            removeFavorite(token, targetType, targetId);
            return false;
        } else {
            addFavorite(token, targetType, targetId);
            return true;
        }
    }

    /**
     * 检查当前用户是否已收藏
     */
    public boolean isFavorited(String token, String targetType, Long targetId) {
        User user = userService.getUserByToken(token);
        if (user == null) return false;
        return favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId);
    }

    /**
     * 根据 targetType 更新对应业务表的 favorite_count
     */
    private void updateFavoriteCount(String targetType, Long targetId, int delta) {
        switch (targetType) {
            case "study":
                if (delta > 0) {
                    studyRepository.incrementFavoriteCount(targetId);
                } else {
                    studyRepository.decrementFavoriteCount(targetId);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取用户收藏列表（分页，带业务摘要）
     */
    public Map<String, Object> getUserFavorites(String token, int pageNo, int pageSize) {
        User user = userService.getUserByToken(token);
        if (user == null) throw new RuntimeException("请先登录");

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Favorite> page = favoriteRepository.findByUserIdOrderByCreateTimeDesc(user.getId(), pageable);

        List<Map<String, Object>> items = page.getContent().stream().map(fav -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", fav.getId());
            map.put("targetType", fav.getTargetType());
            map.put("targetId", fav.getTargetId());
            map.put("createTime", fav.getCreateTime());

            String title = "";
            String brief = "";

            switch (fav.getTargetType()) {
                case "emotion":
                    Emotion emotion = emotionRepository.findById(fav.getTargetId()).orElse(null);
                    if (emotion != null) {
                        String content = emotion.getContent();
                        title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                        brief = content;
                    }
                    break;
                case "quote":
                    Quote quote = quoteRepository.findById(fav.getTargetId()).orElse(null);
                    if (quote != null) {
                        title = "一句名言";
                        brief = quote.getContent();
                    }
                    break;
                case "creed":
                    Creed creed = creedRepository.findById(fav.getTargetId()).orElse(null);
                    if (creed != null) {
                        String content = creed.getContent();
                        title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                        brief = content;
                    }
                    break;
                case "cognize":
                    Cognize cognize = cognizeRepository.findById(fav.getTargetId()).orElse(null);
                    if (cognize != null) {
                        String content = cognize.getContent();
                        title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                        brief = content;
                    }
                    break;
                case "study":
                    // ✅ 使用 getDescription() 而非 getContent()
                    Study study = studyRepository.findById(fav.getTargetId()).orElse(null);
                    if (study != null) {
                        title = study.getTitle();
                        brief = study.getDescription();
                    }
                    break;
                default:
                    title = "未知类型";
                    brief = "该内容暂不支持预览";
                    break;
            }

            map.put("title", title);
            map.put("brief", brief);
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", page.getTotalElements());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }
}