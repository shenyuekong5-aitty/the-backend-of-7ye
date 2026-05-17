package org.example.springboot2.role.repository;

import org.example.springboot2.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    //更新时判断角色是否已经存在用
    boolean existsByName(String name);
}