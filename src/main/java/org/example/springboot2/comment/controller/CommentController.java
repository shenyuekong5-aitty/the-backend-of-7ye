package org.example.springboot2.comment.controller;

import org.example.springboot2.comment.entity.Comment;
import org.example.springboot2.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取评论列表（统一入口）
     * 支持两种场景：
     * 1. 无 targetType 和 targetId → 获取留言板评论（target_type IS NULL）
     * 2. 有 targetType 和 targetId → 获取指定目标的评论
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getCommentList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "token", required = false) String token,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(defaultValue = "false") boolean allComments
            )
    {

        Map<String, Object> data = commentService.getCommentTree(
                pageNo, pageSize, token, targetType, targetId,allComments);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", data);
        response.put("message", "获取评论列表成功");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addComment(
            @RequestHeader("token") String token,
            @RequestBody Map<String, Object> body) {
        try {
            Long parentId = body.containsKey("parentId") ? Long.valueOf(body.get("parentId").toString()) : null;
            String content = (String) body.get("content");
            String targetType = body.containsKey("targetType") ? (String) body.get("targetType") : null;
            Long targetId = body.containsKey("targetId") ? Long.valueOf(body.get("targetId").toString()) : null;

            Comment comment = commentService.addComment(token, parentId, content, targetType, targetId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", comment);
            response.put("message", "发表成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @RequestHeader("token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String newContent = body.get("content");
            Comment comment = commentService.updateComment(token, id, newContent);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", comment);
            response.put("message", "修改成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        try {
            commentService.deleteComment(token, id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.ok(response);
        }
    }
}