package org.example.springboot2.notice.controller;

import org.example.springboot2.notice.entity.Notice;
import org.example.springboot2.notice.service.NoticeService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

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


    /**
     * 获取单条公告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNotice(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", "公告不存在"));
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", notice));
    }

    /**
     * 新增公告（仅管理员）
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addNotice(
            @RequestHeader("token") String token,
            @RequestBody Map<String, Object> body) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Boolean isImportant = body.containsKey("isImportant") ?
                Boolean.valueOf(body.get("isImportant").toString()) : false;

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", 400, "message", "标题不能为空"));
        }

        Notice saved = noticeService.addNotice(title, content,
                user.getNickname() != null ? user.getNickname() : user.getUsername(),
                isImportant);
        return ResponseEntity.ok(Map.of("code", 200, "data", saved, "message", "发布成功"));
    }

    /**
     * 编辑公告（仅管理员）
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateNotice(
            @RequestHeader("token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Boolean isImportant = body.containsKey("isImportant") ?
                Boolean.valueOf(body.get("isImportant").toString()) : null;

        try {
            Notice updated = noticeService.updateNotice(id, title, content, isImportant);
            return ResponseEntity.ok(Map.of("code", 200, "data", updated, "message", "修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }

    /**
     * 删除公告（仅管理员）
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotice(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        try {
            noticeService.deleteNotice(id);
            return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }
}