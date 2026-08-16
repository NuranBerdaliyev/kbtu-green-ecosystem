package com.example.green.domain.repository;

import com.example.green.domain.entity.Authentication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {
    Optional<Authentication> findByToken(String token);
    void deleteAllByUserId(Long userId);
}
