package org.example.springboot2.notice.service;

import org.example.springboot2.notice.entity.Notice;
import org.example.springboot2.notice.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    public Map<String, Object> getNoticeList(int pageNo, int pageSize) {
        // 创建分页请求（按 isImportant 降序，publishTime 降序）
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Order.desc("isImportant"),
                        Sort.Order.desc("publishTime")));

        Page<Notice> page = noticeRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("items", page.getContent());
        response.put("total", page.getTotalElements());
        response.put("pageSize", pageSize);
        response.put("pageNo", pageNo);
        return response;
    }

    /**
     * 根据 ID 获取公告
     */
    public Notice getById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }

    /**
     * 新增公告
     */
    @Transactional
    public Notice addNotice(String title, String content, String publisher, Boolean isImportant) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPublisher(publisher);
        notice.setIsImportant(isImportant != null ? isImportant : false);
        notice.setPublishTime(LocalDateTime.now());
        return noticeRepository.save(notice);
    }

    /**
     * 编辑公告
     */
    @Transactional
    public Notice updateNotice(Long id, String title, String content, Boolean isImportant) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        notice.setTitle(title);
        notice.setContent(content);
        if (isImportant != null) {
            notice.setIsImportant(isImportant);
        }
        return noticeRepository.save(notice);
    }

    /**
     * 删除公告
     */
    @Transactional
    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new RuntimeException("公告不存在");
        }
        noticeRepository.deleteById(id);
    }
}