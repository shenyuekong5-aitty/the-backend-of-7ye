package org.example.springboot2.cognize.repository;

import org.example.springboot2.cognize.entity.Cognize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CognizeRepository extends JpaRepository<Cognize, Long> {
}