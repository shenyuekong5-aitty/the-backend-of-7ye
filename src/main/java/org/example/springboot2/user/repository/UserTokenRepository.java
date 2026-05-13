package org.example.springboot2.user.repository;

import org.example.springboot2.user.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByToken(String token);
    List<UserToken> findByUserIdOrderByCreateTimeAsc(Long userId);
    void deleteByToken(String token);
    int countByUserId(Long userId);
}