package com.daka.server.repository;

import com.daka.server.entity.CheckinPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CheckinPointRepository extends JpaRepository<CheckinPoint, String> {
    List<CheckinPoint> findByUserIdOrderByCreateTimeDesc(String userId);
    List<CheckinPoint> findByUserIdAndCityCode(String userId, String cityCode);
    List<CheckinPoint> findByUserIdAndProvince(String userId, String province);
    long countByUserId(String userId);
}