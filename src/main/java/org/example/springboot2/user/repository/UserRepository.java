package org.example.springboot2.user.repository;

import org.example.springboot2.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByToken(String token);

    boolean existsByUsername(String username);

    long countByRoleId(Long roleId);
}