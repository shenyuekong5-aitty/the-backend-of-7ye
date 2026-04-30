package org.example.springboot2.permission.service;

import org.example.springboot2.permission.entity.RolePermission;
import org.example.springboot2.permission.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    /**
     * 获取某角色的所有权限标识
     */
    public List<String> getPermissionsByRoleId(Long roleId) {
        List<RolePermission> list = rolePermissionRepository.findByRoleId(roleId);
        return list.stream().map(RolePermission::getPermission).collect(Collectors.toList());
    }

    /**
     * 更新角色权限 (全量替换)
     */
    @Transactional
    public void updateRolePermissions(Long roleId, List<String> permissions) {
        rolePermissionRepository.deleteByRoleId(roleId);
        for (String perm : permissions) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermission(perm);
            rolePermissionRepository.save(rp);
        }
    }
}