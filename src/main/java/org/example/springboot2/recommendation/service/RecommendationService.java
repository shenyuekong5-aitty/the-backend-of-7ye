package org.example.springboot2.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot2.anime.entity.Anime;
import org.example.springboot2.anime.repository.AnimeRepository;
import org.example.springboot2.book.entity.Book;
import org.example.springboot2.book.repository.BookRepository;
import org.example.springboot2.music.entity.Music;
import org.example.springboot2.music.repository.MusicRepository;
import org.example.springboot2.recommendation.entity.Recommendation;
import org.example.springboot2.recommendation.repository.RecommendationRepository;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private AnimeRepository animeRepository;   // ✅ 新增番剧仓库

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 朋友提交推荐
    @Transactional
    public Recommendation submit(String token, String type, Map<String, Object> contentMap) {
        User user = userService.getUserByToken(token);
        if ("friend".equals(user.getRole())) {
            throw new RuntimeException("无权限");
        }
        try {
            String json = objectMapper.writeValueAsString(contentMap);
            Recommendation rec = new Recommendation();
            rec.setType(type);
            rec.setContent(json);
            rec.setProposerId(user.getId());
            rec.setProposerName(user.getUsername());
            rec.setStatus("pending");
            return recommendationRepository.save(rec);
        } catch (Exception e) {
            throw new RuntimeException("数据格式错误");
        }
    }

    // 管理员审核通过（支持 book, music, anime）
    @Transactional
    public void approve(String token, Long recId) {
        User admin = userService.getUserByToken(token);
        if (!"admin".equals(admin.getRole())) {
            throw new RuntimeException("无权限");
        }
        Recommendation rec = recommendationRepository.findById(recId)
                .orElseThrow(() -> new RuntimeException("推荐记录不存在"));
        if (!"pending".equals(rec.getStatus())) {
            throw new RuntimeException("该推荐已处理");
        }

        switch (rec.getType()) {
            case "book":
                try {
                    Book book = objectMapper.readValue(rec.getContent(), Book.class);
                    book.setId(null);
                    bookRepository.save(book);
                } catch (Exception e) {
                    throw new RuntimeException("书籍内容解析失败");
                }
                break;
            case "music":
                try {
                    Music music = objectMapper.readValue(rec.getContent(), Music.class);
                    music.setId(null);
                    musicRepository.save(music);
                } catch (Exception e) {
                    throw new RuntimeException("音乐内容解析失败");
                }
                break;
            case "anime":   // ✅ 新增番剧处理
                try {
                    Anime anime = objectMapper.readValue(rec.getContent(), Anime.class);
                    anime.setId(null);
                    animeRepository.save(anime);
                } catch (Exception e) {
                    throw new RuntimeException("番剧内容解析失败");
                }
                break;
            default:
                throw new RuntimeException("不支持的推荐类型: " + rec.getType());
        }

        rec.setStatus("approved");
        rec.setReviewerId(admin.getId());
        rec.setUpdateTime(java.time.LocalDateTime.now());
        recommendationRepository.save(rec);
    }

    // 拒绝推荐
    @Transactional
    public void reject(String token, Long recId, String comment) {
        User admin = userService.getUserByToken(token);
        if (!"admin".equals(admin.getRole())) {
            throw new RuntimeException("无权限");
        }
        Recommendation rec = recommendationRepository.findById(recId)
                .orElseThrow(() -> new RuntimeException("推荐记录不存在"));
        rec.setStatus("rejected");
        rec.setReviewerId(admin.getId());
        rec.setReviewComment(comment);
        rec.setUpdateTime(java.time.LocalDateTime.now());
        recommendationRepository.save(rec);
    }

    // 获取待审核列表（管理员用）
    public List<Map<String, Object>> getPendingList() {
        List<Recommendation> recs = recommendationRepository.findByStatusOrderByCreateTimeDesc("pending");
        return convertToMapList(recs);
    }

    // 根据状态获取推荐列表（若 status 为空或 null，则返回全部）
    public List<Map<String, Object>> getListByStatus(String status) {
        List<Recommendation> recs;
        if (status == null || status.isEmpty()) {
            recs = recommendationRepository.findAllByOrderByCreateTimeDesc();
        } else {
            recs = recommendationRepository.findByStatusOrderByCreateTimeDesc(status);
        }
        return convertToMapList(recs);
    }

    // 提取公共的转换方法
    private List<Map<String, Object>> convertToMapList(List<Recommendation> recs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Recommendation rec : recs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", rec.getId());
            item.put("type", rec.getType());
            item.put("proposerName", rec.getProposerName());
            item.put("status", rec.getStatus());
            item.put("createTime", rec.getCreateTime());
            item.put("reviewComment", rec.getReviewComment());
            try {
                Map<String, Object> content = objectMapper.readValue(rec.getContent(), Map.class);
                item.put("content", content);
            } catch (Exception e) {
                item.put("content", rec.getContent());
            }
            result.add(item);
        }
        return result;
    }
}