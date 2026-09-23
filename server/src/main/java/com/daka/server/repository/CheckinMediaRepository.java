package com.daka.server.repository;

import com.daka.server.entity.CheckinMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CheckinMediaRepository extends JpaRepository<CheckinMedia, String> {
    List<CheckinMedia> findByCheckinId(String checkinId);
    void deleteByCheckinId(String checkinId);
}