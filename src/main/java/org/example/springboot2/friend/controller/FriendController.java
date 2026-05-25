package org.example.springboot2.friend.controller;

import lombok.RequiredArgsConstructor;
import org.example.springboot2.friend.entity.FriendMemory;
import org.example.springboot2.friend.service.FriendService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        // 保持不变
        User currentUser = getCurrentUser(token);
        if (currentUser == null) return unauthorized();

        Map<String, Object> data = new HashMap<>();
        if ("admin".equals(currentUser.getRole())) {
            data.put("partnerNickname", "所有朋友");
        } else {
            User adminUser = userService.getUserById(1L);
            data.put("partnerNickname", adminUser != null ? adminUser.getNickname() : "核心用户");
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", data));
    }

    // ----- 获取专属回忆（支持分页） -----
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemories(
            @RequestHeader("token") String token,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = getCurrentUser(token);
        if (currentUser == null) return unauthorized();

        Long myId = currentUser.getId();
        Long targetFriendId;

        if ("admin".equals(currentUser.getRole())) {
            if (partnerId != null) {
                targetFriendId = partnerId;
            } else {
                // 未选择朋友，返回空的分页结构
                Map<String, Object> data = new HashMap<>();
                data.put("items", List.of());
                data.put("totalPages", 0);
                data.put("totalElements", 0);
                data.put("currentPage", 0);
                data.put("size", size);
                return ResponseEntity.ok(Map.of("code", 200, "data", data, "message", "请选择朋友"));
            }
        } else {
            targetFriendId = 1L;  // 核心用户ID
        }

        Page<FriendMemory> memoryPage = friendService.getMemoriesBetween(myId, targetFriendId, PageRequest.of(page, size));
        List<Map<String, Object>> result = memoryPage.getContent().stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("userId", m.getUserId());
            map.put("friendId", m.getFriendId());
            map.put("title", m.getTitle());
            map.put("photo", m.getPhoto());
            map.put("description", m.getDescription());
            map.put("memoryTime", m.getMemoryTime());
            map.put("createTime", m.getCreateTime());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("items", result);
        data.put("totalPages", memoryPage.getTotalPages());
        data.put("totalElements", memoryPage.getTotalElements());
        data.put("currentPage", memoryPage.getNumber());
        data.put("size", memoryPage.getSize());

        return ResponseEntity.ok(Map.of("code", 200, "data", data, "message", "获取成功"));
    }

    // ----- 添加回忆（双方均可） -----
    @PostMapping("/memory")
    public ResponseEntity<Map<String, Object>> addMemory(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {
        // 保持不变
        User currentUser = getCurrentUser(token);
        if (currentUser == null) return unauthorized();

        Long friendId;
        if ("admin".equals(currentUser.getRole())) {
            friendId = Long.parseLong(body.get("friendId"));
        } else {
            friendId = 1L;
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
        // 保持不变
        User currentUser = getCurrentUser(token);
        if (currentUser == null) return unauthorized();
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