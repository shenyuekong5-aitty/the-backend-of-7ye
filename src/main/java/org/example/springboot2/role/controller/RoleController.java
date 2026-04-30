package org.example.springboot2.role.controller;

import org.example.springboot2.role.entity.Role;
import org.example.springboot2.role.service.RoleService;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list() {
        List<Role> roles = roleService.getAllRoles();
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("data", roles);
        return ResponseEntity.ok(resp);
    }
    @PostMapping
    public ResponseEntity<Map<String, Object>> addRole(@RequestBody Role role) {
        // 简单保存，实际可增加名称唯一校验
        roleService.saveRole(role);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("message", "角色添加成功");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        Role existing = roleService.getRoleById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", 404, "message", "角色不存在"));
        }
        existing.setName(role.getName());
        existing.setDescription(role.getDescription());
        roleService.saveRole(existing);
        return ResponseEntity.ok(Map.of("code", 200, "message", "角色更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        // 检查是否有用户使用该角色
        long userCount = userService.countUsersByRoleId(id);
        if (userCount > 0) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "该角色正在使用中，无法删除"));
        }
        roleService.deleteRole(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "角色删除成功"));
    }
}