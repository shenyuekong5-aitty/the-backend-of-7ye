package org.example.springboot2.qrlogin.controller;

import org.example.springboot2.qrlogin.entity.QrSession;
import org.example.springboot2.qrlogin.service.QrLoginService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qrlogin")
public class QrLoginController {

    @Autowired
    private QrLoginService qrLoginService;

    @Autowired
    private UserService userService;

    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate() {
        String sessionId = qrLoginService.generateSession();
        return ResponseEntity.ok(Map.of("code", 200, "sessionId", sessionId));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@RequestParam String sessionId) {
        QrSession session = qrLoginService.getSession(sessionId);
        if (session == null || "EXPIRED".equals(session.getStatus())) {
            return ResponseEntity.ok(Map.of("code", 200, "status", "EXPIRED"));
        }
        if ("CONFIRMED".equals(session.getStatus())) {
            String token = qrLoginService.getConfirmedToken(sessionId);
            return ResponseEntity.ok(Map.of("code", 200, "status", "CONFIRMED", "token", token));
        }
        return ResponseEntity.ok(Map.of("code", 200, "status", "WAITING"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {

        //  从 JSON 请求体中取出 sessionId
        String sessionId = body.get("sessionId");
        System.out.println("接收到的token:" + token);
        System.out.println("接收到的sessionId:" + sessionId);
        if (sessionId == null || sessionId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "缺少 sessionId"));
        }

        User user = userService.getUserByToken(token);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "未登录"));
        }

        qrLoginService.confirmSession(sessionId, user.getId());
        return ResponseEntity.ok(Map.of("code", 200, "message", "已确认"));
    }
}