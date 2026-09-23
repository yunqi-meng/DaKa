package com.daka.server.repository;

import com.daka.server.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProgressRepository extends JpaRepository<UserProgress, String> {
}