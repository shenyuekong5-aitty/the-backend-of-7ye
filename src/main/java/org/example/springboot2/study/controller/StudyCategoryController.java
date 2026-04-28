package org.example.springboot2.study.controller;

import org.example.springboot2.study.entity.StudyCategory;
import org.example.springboot2.study.service.StudyCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study/category")
public class StudyCategoryController {

    @Autowired
    private StudyCategoryService categoryService;

    /** 获取分类列表，parentId 可选；不传返回一级分类，传了返回对应的子分类 */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) Long parentId) {
        List<StudyCategory> categories = categoryService.getCategories(parentId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", categories);
        return ResponseEntity.ok(response);
    }
}