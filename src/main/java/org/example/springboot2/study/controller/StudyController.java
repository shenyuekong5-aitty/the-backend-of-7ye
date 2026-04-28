package org.example.springboot2.study.controller;

import org.example.springboot2.study.entity.Study;
import org.example.springboot2.study.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    @Autowired
    private StudyService studyService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getStudyList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long parentCategoryId) {   // ✅ 新增
        Map<String, Object> data = studyService.getStudyList(pageNo, pageSize, categoryId, parentCategoryId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", data);
        response.put("message", "获取学习列表成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudyById(@PathVariable Long id) {
        Study study = studyService.getStudyById(id);
        Map<String, Object> response = new HashMap<>();
        if (study == null) {
            response.put("code", 404);
            response.put("message", "条目不存在");
            return ResponseEntity.ok(response);
        }
        studyService.viewStudy(id);
        response.put("code", 200);
        response.put("data", study);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addStudy(
            @RequestHeader("token") String token,
            @RequestBody Study study) {
        try {
            Study saved = studyService.addStudy(token, study);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", saved);
            response.put("message", "创建成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateStudy(
            @RequestHeader("token") String token,
            @RequestBody Study study) {
        try {
            Study updated = studyService.updateStudy(token, study);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", updated);
            response.put("message", "更新成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteStudy(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        try {
            studyService.deleteStudy(token, id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}