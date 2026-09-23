package com.daka.server.repository;

import com.daka.server.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, String> {
    List<Achievement> findByUserId(String userId);
    List<Achievement> findByUserIdAndIsUnlockedTrue(String userId);
}