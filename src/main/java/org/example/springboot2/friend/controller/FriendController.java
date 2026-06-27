package org.example.springboot2.friend.controller;

import lombok.RequiredArgsConstructor;
import org.example.springboot2.friend.entity.FriendMemory;
import org.example.springboot2.friend.service.FriendService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        Pageable pageable = PageRequest.of(page, size);
        Page<FriendMemory> memoryPage;

        if ("admin".equals(currentUser.getRole())) {
            if (partnerId != null) {
                if (partnerId == 3L || partnerId == 4L) {
                    List<Long> friendIds = new ArrayList<>();
                    friendIds.add(3L);
                    friendIds.add(4L);
                    memoryPage = friendService.getMemoriesWithFriends(myId, friendIds, pageable);
                } else {
                    memoryPage = friendService.getMemoriesBetween(myId, partnerId, pageable);
                };
            } else {
                // 未选择朋友，返回空分页
                Map<String, Object> data = new HashMap<>();
                data.put("items", List.of());
                data.put("totalPages", 0);
                data.put("totalElements", 0);
                data.put("currentPage", 0);
                data.put("size", size);
                return ResponseEntity.ok(Map.of("code", 200, "data", data, "message", "请选择朋友"));
            }
        }  else {
        // 普通朋友：查看自己与另一个朋友（如果特殊规则）的所有回忆
        List<Long> participantIds = new ArrayList<>();
        participantIds.add(myId);            // 自己
        if (myId == 3L) {
            participantIds.add(4L);          // 如果我是 A，也包含 B
        } else if (myId == 4L) {
            participantIds.add(3L);          // 如果我是 B，也包含 A
        }
        // 注意：不再手动添加管理员 1L，因为管理员参与的回忆已经包含在参与者条件里
        memoryPage = friendService.getMemoriesByParticipants(participantIds, pageable);
    }

        // 转换为 Map 列表
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