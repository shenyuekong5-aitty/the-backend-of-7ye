package org.example.springboot2.friend.controller;

import lombok.RequiredArgsConstructor;
import org.example.springboot2.friend.entity.FriendMemory;
import org.example.springboot2.friend.service.FriendService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    private User getCurrentUser(String token) {
        return userService.getUserByToken(token);
    }

    // ----- 获取当前用户的核心好友昵称（用于显示标题） -----
    @GetMapping("/partner/info")
    public ResponseEntity<Map<String, Object>> getPartnerInfo(@RequestHeader("token") String token) {
        User currentUser = getCurrentUser(token);
        if (currentUser == null) return unauthorized();

        // 如果当前用户是管理员，可以指定查看某个朋友的昵称，否则固定返回管理员的昵称
        // 这里简单处理：朋友角色总是显示管理员昵称，管理员则返回一个默认
        Map<String, Object> data = new HashMap<>();
        if ("admin".equals(currentUser.getRole())) {
            data.put("partnerNickname", "所有朋友");  // 管理员可能看多个，暂时用通用标题
        } else {
            // 查找管理员，假设管理员 id=1
            User adminUser = userService.getUserById(1L);
            data.put("partnerNickname", adminUser != null ? adminUser.getNickname() : "核心用户");
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", data));
    }

    // ----- 获取专属回忆（管理员可以指定 partnerId，朋友只能看自己与管理员的） -----
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemories(
            @RequestHeader("token") String token,
            @RequestParam(required = false) Long partnerId) {
        User currentUser = getCurrentUser(token);
        if (currentUser == null) return unauthorized();

        Long myId = currentUser.getId();
        Long targetFriendId;

        if ("admin".equals(currentUser.getRole())) {
            // 管理员可以指定查看哪个朋友的回忆，不传则返回空或所有
            if (partnerId != null) {
                targetFriendId = partnerId;
            } else {
                // 管理员未指定，暂时返回空列表，或可返回所有朋友的总回忆（略）
                return ResponseEntity.ok(Map.of("code", 200, "data", Map.of("items", List.of()), "message", "请选择朋友"));
            }
        } else {
            // 朋友角色：只允许看自己与管理员的回忆，管理员id固定为1（可从配置获取）
            targetFriendId = 1L;  // 核心用户ID
        }

        List<FriendMemory> memories = friendService.getMemoriesBetween(myId, targetFriendId);
        List<Map<String, Object>> result = memories.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("userId", m.getUserId());
            map.put("friendId", m.getFriendId());
            map.put("title", m.getTitle());
            map.put("photo", m.getPhoto());
            map.put("description", m.getDescription());
            map.put("memoryTime", m.getMemoryTime());
            map.put("createTime", m.getCreateTime());
            // 可添加作者昵称等信息
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("code", 200, "data", Map.of("items", result), "message", "获取成功"));
    }

    // ----- 添加回忆（双方均可） -----
    @PostMapping("/memory")
    public ResponseEntity<Map<String, Object>> addMemory(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {
        User currentUser = getCurrentUser(token);
        if (currentUser == null) return unauthorized();

        Long friendId;
        if ("admin".equals(currentUser.getRole())) {
            friendId = Long.parseLong(body.get("friendId"));
        } else {
            friendId = 1L; // 朋友只能和管理员创建回忆
        }
        String title = body.get("title");
        String photo = body.get("photo");
        String description = body.get("description");
        String memoryTime = body.get("memoryTime");
        try {
            FriendMemory memory = friendService.addMemory(currentUser.getId(), friendId, title, photo, description, memoryTime);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 200);
            resp.put("data", memory);
            resp.put("message", "添加成功");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        }
    }

    // ----- 删除回忆 -----
    @DeleteMapping("/memory/{memoryId}")
    public ResponseEntity<Map<String, Object>> deleteMemory(
            @RequestHeader("token") String token,
            @PathVariable Long memoryId) {
        User currentUser = getCurrentUser(token);
        if (currentUser == null) return unauthorized();
        // 权限可在 service 中进一步校验
        friendService.deleteMemory(memoryId);
        return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
    }

    private ResponseEntity<Map<String, Object>> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("code", 401, "message", "未登录"));
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getFriendUsers(@RequestHeader("token") String token) {
        List<Map<String, Object>> users = friendService.getFriendUsers();
        return ResponseEntity.ok(Map.of("code", 200, "data", Map.of("items", users)));
    }
}