package org.example.springboot2.role.controller;

import org.example.springboot2.role.entity.Role;
import org.example.springboot2.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list() {
        List<Role> roles = roleService.getAllRoles();
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("data", roles);
        return ResponseEntity.ok(resp);
    }
}