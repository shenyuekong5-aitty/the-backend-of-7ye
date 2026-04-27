package org.example.springboot2.emotion.controller;

import org.example.springboot2.emotion.entity.Emotion;
import org.example.springboot2.emotion.service.EmotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emotion")
public class EmotionController {

    @Autowired
    private EmotionService emotionService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getEmotionList() {
        List<Emotion> emotions = emotionService.getAllEmotions();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", emotions);
        data.put("message", "获取心理列表成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}