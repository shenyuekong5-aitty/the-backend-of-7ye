package org.example.springboot2.user.repository;

import org.example.springboot2.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    User findByPhone(String phone);
    User findByToken(String token);

    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);

    boolean existsByPhoneAndStatusNot(String phone, String status);

    boolean existsByUsernameAndStatusNot(String username, String status);

    User findByUsernameAndStatus(String username, String status);
    User findByPhoneAndStatus(String phone, String status);

    long countByRoleId(Long roleId);
    List<User> findByStatusAndDeletedAtBefore(String status, LocalDateTime deletedAt);

    List<User> findByRole(String role);
}