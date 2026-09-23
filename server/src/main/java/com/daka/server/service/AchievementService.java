package com.daka.server.service;

import com.daka.server.dto.AchievementDto;
import com.daka.server.dto.UserProgressDto;
import com.daka.server.entity.Achievement;
import com.daka.server.entity.UserProgress;
import com.daka.server.repository.AchievementRepository;
import com.daka.server.repository.UserProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepo;
    private final UserProgressRepository progressRepo;

    public AchievementService(AchievementRepository achievementRepo, UserProgressRepository progressRepo) {
        this.achievementRepo = achievementRepo;
        this.progressRepo = progressRepo;
    }

    public List<AchievementDto> listByUser(String userId) {
        return achievementRepo.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(String userId, AchievementDto dto) {
        String id = dto.getId() == null || dto.getId().isBlank()
                ? UUID.randomUUID().toString() : dto.getId();
        Achievement existing = achievementRepo.findById(id).orElse(null);
        if (existing != null && !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改该成就");
        }
        Achievement a = existing != null ? existing : new Achievement();
        a.setId(id);
        a.setName(dto.getName());
        a.setDescription(dto.getDescription());
        a.setCategory(dto.getCategory());
        a.setTarget(dto.getTarget());
        a.setIsUnlocked(dto.getIsUnlocked());
        a.setUnlockTime(dto.getUnlockTime());
        a.setProgress(dto.getProgress());
        a.setUserId(userId);
        achievementRepo.save(a);
    }

    @Transactional
    public void updateProgress(String userId, UserProgressDto dto) {
        UserProgress p = progressRepo.findById(userId).orElseGet(() -> {
            UserProgress np = new UserProgress();
            np.setUserId(userId);
            return np;
        });
        if (dto.getTotalCheckinCount() != null) p.setTotalCheckinCount(dto.getTotalCheckinCount());
        if (dto.getTotalRevisitProgress() != null) p.setTotalRevisitProgress(dto.getTotalRevisitProgress());
        if (dto.getActivatedProvinceCount() != null) p.setActivatedProvinceCount(dto.getActivatedProvinceCount());
        if (dto.getActivatedCityCount() != null) p.setActivatedCityCount(dto.getActivatedCityCount());
        progressRepo.save(p);
    }

    public UserProgressDto getProgress(String userId) {
        UserProgress p = progressRepo.findById(userId).orElseGet(() -> {
            UserProgress np = new UserProgress();
            np.setUserId(userId);
            return progressRepo.save(np);
        });
        UserProgressDto dto = new UserProgressDto();
        dto.setUserId(p.getUserId());
        dto.setTotalCheckinCount(p.getTotalCheckinCount());
        dto.setTotalRevisitProgress(p.getTotalRevisitProgress());
        dto.setActivatedProvinceCount(p.getActivatedProvinceCount());
        dto.setActivatedCityCount(p.getActivatedCityCount());
        return dto;
    }

    private AchievementDto toDto(Achievement a) {
        AchievementDto dto = new AchievementDto();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setDescription(a.getDescription());
        dto.setCategory(a.getCategory());
        dto.setTarget(a.getTarget());
        dto.setIsUnlocked(a.getIsUnlocked());
        dto.setUnlockTime(a.getUnlockTime());
        dto.setProgress(a.getProgress());
        return dto;
    }
}