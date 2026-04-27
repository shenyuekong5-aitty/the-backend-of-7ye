package org.example.springboot2.notice.controller;

import org.example.springboot2.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getNoticeList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "6") int pageSize) {

        Map<String, Object> data = noticeService.getNoticeList(pageNo, pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "ok");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}