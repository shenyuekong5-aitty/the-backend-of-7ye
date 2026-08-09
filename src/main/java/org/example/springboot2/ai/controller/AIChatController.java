package org.example.springboot2.ai.controller;

import org.example.springboot2.ai.model.ChatRequest;
import org.example.springboot2.ai.model.ChatResponse;
import org.example.springboot2.ai.service.AIChatService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIChatController {

    @Autowired
    private AIChatService aiChatService;

    @Autowired
    private UserService userService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "token", required = false) String token) {

        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 400);
            resp.put("message", "question 不能为空");
            return ResponseEntity.badRequest().body(resp);
        }

        String userId = null;
        if (token != null && !token.isBlank()) {
            User user = userService.getUserByToken(token);
            if (user != null) {
                userId = String.valueOf(user.getId());
            }
        }

        try {
            ChatResponse response = aiChatService.chat(request, userId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 200);
            resp.put("data", response);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 400);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 500);
            resp.put("message", "AI 服务异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        }
    }
}