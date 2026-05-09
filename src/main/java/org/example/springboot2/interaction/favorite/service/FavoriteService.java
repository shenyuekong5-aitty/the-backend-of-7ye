package org.example.springboot2.interaction.favorite.service;

import org.example.springboot2.emotion.entity.Emotion;
import org.example.springboot2.favorite.entity.Favorite;
import org.example.springboot2.interaction.favorite.repository.FavoriteRepository;
import org.example.springboot2.quote.entity.Quote;
import org.example.springboot2.study.repository.StudyRepository;   // 示例，可注入其他业务 Repository
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.springboot2.emotion.repository.EmotionRepository;
import org.example.springboot2.quote.repository.QuoteRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyRepository studyRepository;   // 学习模块的 Repository，用于更新 favorite_count

    @Autowired
    private EmotionRepository emotionRepository;

    @Autowired
    private QuoteRepository quoteRepository;

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

        // 更新对应业务表的 favorite_count（以 study 为例）
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

        // 更新对应业务表的 favorite_count
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
            // case "book": ...
            // case "music": ...
            default:
                // 可记录日志或忽略
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

            // 根据 targetType 获取业务摘要
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