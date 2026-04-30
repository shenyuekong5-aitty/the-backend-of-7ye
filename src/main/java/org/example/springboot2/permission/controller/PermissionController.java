package org.example.springboot2.permission.controller;

import org.example.springboot2.permission.repository.RolePermissionRepository;
import org.example.springboot2.permission.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listAllPermissions() {
        List<String> allPermissions = rolePermissionRepository.findAllDistinctPermissions();
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("data", allPermissions);
        return ResponseEntity.ok(resp);
    }
    /**
     * 获取指定角色的权限列表
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<Map<String, Object>> getPermissions(@PathVariable Long roleId) {
        List<String> permissions = permissionService.getPermissionsByRoleId(roleId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("data", permissions);
        return ResponseEntity.ok(resp);
    }

    /**
     * 更新指定角色的权限 (请求体传权限数组)
     */
    @PutMapping("/role/{roleId}")
    public ResponseEntity<Map<String, Object>> updatePermissions(
            @PathVariable Long roleId,
            @RequestBody List<String> permissions) {
        permissionService.updateRolePermissions(roleId, permissions);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("message", "权限更新成功");
        return ResponseEntity.ok(resp);
    }
}