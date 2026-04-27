package org.example.springboot2.interaction.like.controller;

import org.example.springboot2.interaction.like.service.LikeService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞控制器
 * 负责处理点赞/取消点赞以及查询点赞状态的 HTTP 请求
 */
@RestController
@RequestMapping("/api/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;   // 用于从 token 获取当前登录用户

    /**
     * 切换点赞状态（点赞/取消点赞）
     *
     * @param token 请求头中的认证令牌，用于识别当前用户
     * @param body  请求体，需包含两个字段：
     *              targetType - 点赞对象类型（如 "study", "comment"）
     *              targetId   - 点赞对象 ID
     * @return 统一响应体：
     *         code: 200,
     *         data: { liked: boolean },  // true 表示操作后已点赞，false 表示已取消点赞
     *         message: 相应的提示信息
     */
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {
        // 提取参数
        String targetType = body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId"));

        // 调用服务层执行点赞/取消点赞逻辑
        boolean liked = likeService.toggleLike(token, targetType, targetId);

        // 组装返回信息
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("liked", liked));
        response.put("message", liked ? "点赞成功" : "已取消点赞");
        return ResponseEntity.ok(response);
    }

    /**
     * 检查当前登录用户是否对指定对象已点赞
     *
     * @param token      请求头中的认证令牌
     * @param targetType 点赞对象类型
     * @param targetId   点赞对象 ID
     * @return 统一响应体：
     *         code: 200,
     *         data: { liked: boolean }  // 当前用户是否已点赞
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkLike(
            @RequestHeader("token") String token,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        // 根据 token 获取用户，未能获取则视为未登录
        User user = userService.getUserByToken(token);
        // 只有登录用户才查询点赞状态，否则返回 false
        boolean liked = user != null && likeService.isLiked(user.getId(), targetType, targetId);

        return ResponseEntity.ok(Map.of("code", 200, "data", Map.of("liked", liked)));
    }
}