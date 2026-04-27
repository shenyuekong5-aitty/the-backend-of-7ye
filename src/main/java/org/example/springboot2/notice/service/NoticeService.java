package org.example.springboot2.notice.service;

import org.example.springboot2.notice.entity.Notice;
import org.example.springboot2.notice.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}