package org.example.springboot2.cognize.service;

import org.example.springboot2.cognize.entity.Cognize;
import org.example.springboot2.cognize.repository.CognizeRepository;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CognizeService {

    @Autowired
    private CognizeRepository cognizeRepository;

    @Autowired
    private UserService userService;

    public Map<String, Object> getCognizeList(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Cognize> page = cognizeRepository.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("items", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    public Cognize getCognizeById(Long id) {
        return cognizeRepository.findById(id).orElse(null);
    }

    @Transactional
    public Cognize addCognize(String token, Cognize cognize) {
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        cognize.setId(null);
        cognize.setAuthorId(user.getId());
        cognize.setAuthorName(user.getUsername());
        // createTime / updateTime 由 @PrePersist 自动填充
        return cognizeRepository.save(cognize);
    }

    @Transactional
    public Cognize updateCognize(String token, Cognize cognize) {
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        Cognize existing = cognizeRepository.findById(cognize.getId())
                .orElseThrow(() -> new RuntimeException("认知条目不存在"));

        boolean isAdmin = "admin".equals(user.getRole());
        boolean isAuthor = existing.getAuthorId().equals(user.getId());
        if (!isAdmin && !isAuthor) {
            throw new RuntimeException("无权修改他人的认知条目");
        }

        existing.setTitle(cognize.getTitle());
        existing.setContent(cognize.getContent());
        // updateTime 由 @PreUpdate 自动更新
        return cognizeRepository.save(existing);
    }

    @Transactional
    public void deleteCognize(String token, Long id) {
        User user = userService.getUserByToken(token);
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        Cognize existing = cognizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("认知条目不存在"));

        boolean isAdmin = "admin".equals(user.getRole());
        boolean isAuthor = existing.getAuthorId().equals(user.getId());
        if (!isAdmin && !isAuthor) {
            throw new RuntimeException("无权删除他人的认知条目");
        }
        cognizeRepository.delete(existing);
    }
}