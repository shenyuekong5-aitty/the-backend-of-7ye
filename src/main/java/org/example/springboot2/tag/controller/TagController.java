package org.example.springboot2.tag.controller;

import com.alibaba.excel.EasyExcel;
import org.example.springboot2.tag.dto.TagExcelDTO;
import org.example.springboot2.tag.entity.Tag;
import org.example.springboot2.tag.service.TagService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    // 获取所有标签（公开）
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getList() {
        List<Tag> tags = tagService.getAllTags();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("items", tags));
        response.put("message", "获取成功");
        return ResponseEntity.ok(response);
    }

    // 新增标签（仅管理员）
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addTag(
            @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", 400, "message", "内容不能为空"));
        }
        Tag saved = tagService.addTag(content);
        return ResponseEntity.ok(Map.of("code", 200, "data", saved, "message", "添加成功"));
    }

    // 修改标签（仅管理员）
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateTag(
            @RequestHeader("token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        String newContent = body.get("content");
        if (newContent == null || newContent.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", 400, "message", "内容不能为空"));
        }
        try {
            Tag updated = tagService.updateTag(id, newContent);
            return ResponseEntity.ok(Map.of("code", 200, "data", updated, "message", "修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }

    // 删除标签（仅管理员）
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteTag(
            @RequestHeader("token") String token,
            @PathVariable Long id) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        try {
            tagService.deleteTag(id);
            return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }

    /**
     * 导出标签 Excel（仅管理员）
     */
    @GetMapping("/export")
    public void exportTags(@RequestHeader("token") String token, HttpServletResponse response) throws IOException {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            response.setStatus(403);
            response.getWriter().write("无权限");
            return;
        }
        tagService.exportTags(response);
    }

    /**
     * 导入标签 Excel（仅管理员）
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importTags(
            @RequestHeader("token") String token,
            @RequestParam("file") MultipartFile file) {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", "无权限"));
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "文件为空"));
        }
        try {
            Map<String, Object> result = tagService.importTags(file);
            return ResponseEntity.ok(Map.of("code", 200, "data", result, "message", "导入完成"));
        } catch (Exception e) {   // ⚠️ 从 IOException 改为 Exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "文件解析失败，请检查Excel格式是否正确（ID列必须为数字）"));
        }
    }
    /**
     * 下载导入模板（仅管理员）
     */
    @GetMapping("/template")
    public void downloadTemplate(@RequestHeader("token") String token,
                                 HttpServletResponse response) throws IOException {
        User user = userService.getUserByToken(token);
        if (user == null || !"admin".equals(user.getRole())) {
            response.setStatus(403);
            response.getWriter().write("无权限");
            return;
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("标签导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 生成只包含表头的空 Excel（可以加一行示例数据）
        List<TagExcelDTO> templateList = new ArrayList<>();
        // 可选：添加一行示例，帮助用户理解
        TagExcelDTO example = new TagExcelDTO();
        example.setId(1L);      // 新增时可留空或不写，这里示范更新或新增
        example.setContent("示例标签");
        templateList.add(example);

        EasyExcel.write(response.getOutputStream(), TagExcelDTO.class)
                .sheet("标签模板")
                .doWrite(templateList);
    }
}