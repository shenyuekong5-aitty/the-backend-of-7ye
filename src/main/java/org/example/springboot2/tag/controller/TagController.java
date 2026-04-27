package org.example.springboot2.tag.controller;

import org.example.springboot2.tag.entity.Tag;
import org.example.springboot2.tag.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getTagList() {
        List<Tag> tags = tagService.getAllTags();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", tags);
        data.put("message", "获取标签列表成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}