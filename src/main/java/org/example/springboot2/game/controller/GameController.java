package org.example.springboot2.game.controller;

import org.example.springboot2.game.entity.Game;
import org.example.springboot2.game.service.GameService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;   // 注入用户服务用于权限校验

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getGameList() {
        List<Game> games = gameService.getAllGames();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", games);
        response.put("data", data);
        response.put("message", "获取游戏列表成功");
        return ResponseEntity.ok(response);
    }

    // 新增游戏（仅 admin）
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addGame(
            @RequestHeader("token") String token,
            @RequestBody Game game) {
        Map<String, Object> response = new HashMap<>();
        try {
            User currentUser = userService.getUserByToken(token);
            if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                response.put("code", 403);
                response.put("message", "无权限操作");
                return ResponseEntity.status(403).body(response);
            }
            Game saved = gameService.addGame(game);
            response.put("code", 200);
            response.put("data", saved);
            response.put("message", "添加成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 修改游戏（仅 admin）
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateGame(
            @RequestHeader("token") String token,
            @PathVariable Long id,
            @RequestBody Game game) {
        Map<String, Object> response = new HashMap<>();
        try {
            User currentUser = userService.getUserByToken(token);
            if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                response.put("code", 403);
                response.put("message", "无权限操作");
                return ResponseEntity.status(403).body(response);
            }
            Game updated = gameService.updateGame(id, game);
            response.put("code", 200);
            response.put("data", updated);
            response.put("message", "修改成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 删除游戏（仅 admin）
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteGame(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User currentUser = userService.getUserByToken(token);
            if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                response.put("code", 403);
                response.put("message", "无权限操作");
                return ResponseEntity.status(403).body(response);
            }
            gameService.deleteGame(id);
            response.put("code", 200);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}