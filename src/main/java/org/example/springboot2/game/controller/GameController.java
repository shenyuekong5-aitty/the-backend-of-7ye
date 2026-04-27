package org.example.springboot2.game.controller;

import org.example.springboot2.game.entity.Game;
import org.example.springboot2.game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getGameList() {
        List<Game> games = gameService.getAllGames();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", games);
        data.put("message", "获取游戏列表成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}