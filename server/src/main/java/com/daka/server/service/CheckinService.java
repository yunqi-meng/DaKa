package com.daka.server.service;

import com.daka.server.dto.CheckinDto;
import com.daka.server.dto.MediaDto;
import com.daka.server.entity.CheckinMedia;
import com.daka.server.entity.CheckinPoint;
import com.daka.server.entity.UserProgress;
import com.daka.server.repository.CheckinMediaRepository;
import com.daka.server.repository.CheckinPointRepository;
import com.daka.server.repository.UserProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CheckinService {

    private final CheckinPointRepository checkinRepo;
    private final CheckinMediaRepository mediaRepo;
    private final UserProgressRepository progressRepo;

    public CheckinService(CheckinPointRepository checkinRepo, CheckinMediaRepository mediaRepo,
                          UserProgressRepository progressRepo) {
        this.checkinRepo = checkinRepo;
        this.mediaRepo = mediaRepo;
        this.progressRepo = progressRepo;
    }

    public List<CheckinDto> listByUser(String userId) {
        return checkinRepo.findByUserIdOrderByCreateTimeDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CheckinDto save(String userId, CheckinDto dto) {
        String id = dto.getId() == null || dto.getId().isBlank()
                ? UUID.randomUUID().toString() : dto.getId();
        CheckinPoint existing = checkinRepo.findById(id).orElse(null);
        if (existing != null && !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改该打卡记录");
        }
        CheckinPoint point = existing != null ? existing : new CheckinPoint();
        point.setId(id);
        point.setLatitude(dto.getLatitude());
        point.setLongitude(dto.getLongitude());
        point.setProvince(dto.getProvince());
        point.setCityCode(dto.getCityCode());
        point.setCity(dto.getCity());
        point.setDistrict(dto.getDistrict());
        point.setAddress(dto.getAddress());
        point.setTitle(dto.getTitle());
        point.setContent(dto.getContent());
        point.setUnlockMode(dto.getUnlockMode());
        point.setRevisitCount(dto.getRevisitCount());
        point.setIsForceUnlocked(dto.getIsForceUnlocked());
        point.setCreateTime(dto.getCreateTime());
        point.setUpdateTime(System.currentTimeMillis());
        point.setUserId(userId);

        if (dto.getMedia() != null) {
            List<CheckinMedia> mediaList = new ArrayList<>();
            for (MediaDto m : dto.getMedia()) {
                CheckinMedia media = new CheckinMedia();
                media.setId(m.getId());
                media.setCheckinId(id);
                media.setMediaType(m.getMediaType());
                media.setRemoteUrl(m.getRemoteUrl());
                mediaList.add(media);
            }
            mediaRepo.deleteByCheckinId(id);
            mediaRepo.saveAll(mediaList);
        }
        checkinRepo.save(point);
        updateProgress(userId);
        return toDto(point);
    }

    @Transactional
    public void delete(String userId, String id) {
        checkinRepo.findById(id).ifPresent(point -> {
            if (!point.getUserId().equals(userId)) {
                throw new IllegalArgumentException("无权删除该打卡记录");
            }
            mediaRepo.deleteByCheckinId(id);
            checkinRepo.deleteById(id);
            updateProgress(userId);
        });
    }

    private void updateProgress(String userId) {
        UserProgress progress = progressRepo.findById(userId).orElseGet(() -> {
            UserProgress p = new UserProgress();
            p.setUserId(userId);
            return p;
        });
        progress.setTotalCheckinCount((int) checkinRepo.countByUserId(userId));
        progress.setUpdateTime(System.currentTimeMillis());
        progressRepo.save(progress);
    }

    private CheckinDto toDto(CheckinPoint point) {
        CheckinDto dto = new CheckinDto();
        dto.setId(point.getId());
        dto.setLatitude(point.getLatitude());
        dto.setLongitude(point.getLongitude());
        dto.setProvince(point.getProvince());
        dto.setCityCode(point.getCityCode());
        dto.setCity(point.getCity());
        dto.setDistrict(point.getDistrict());
        dto.setAddress(point.getAddress());
        dto.setTitle(point.getTitle());
        dto.setContent(point.getContent());
        dto.setUnlockMode(point.getUnlockMode());
        dto.setRevisitCount(point.getRevisitCount());
        dto.setIsForceUnlocked(point.getIsForceUnlocked());
        dto.setCreateTime(point.getCreateTime());
        dto.setUpdateTime(point.getUpdateTime());
        return dto;
    }
}