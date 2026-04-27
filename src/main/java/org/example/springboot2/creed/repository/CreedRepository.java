package org.example.springboot2.creed.repository;

import org.example.springboot2.creed.entity.Creed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreedRepository extends JpaRepository<Creed, Long> {
}