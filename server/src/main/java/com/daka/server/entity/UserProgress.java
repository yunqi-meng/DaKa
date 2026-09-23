package com.daka.server.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_progress")
public class UserProgress {
    @Id
    private String userId;

    private Integer totalCheckinCount = 0;
    private Integer totalRevisitProgress = 0;
    private Integer activatedProvinceCount = 0;
    private Integer activatedCityCount = 0;
    private Integer totalPhotoCount = 0;
    private Long updateTime = System.currentTimeMillis();
}