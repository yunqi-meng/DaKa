package com.daka.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "checkin_point")
public class CheckinPoint {
    @Id
    private String id;

    private Double latitude;
    private Double longitude;
    private String province;
    private String cityCode;
    private String city;
    private String district;
    private String address;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private Integer unlockMode = 0;
    private Integer revisitCount = 0;
    private Long lastEnterTime = 0L;
    private Boolean isForceUnlocked = false;
    private Long createTime = System.currentTimeMillis();
    private Long updateTime = System.currentTimeMillis();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "checkinId")
    private List<CheckinMedia> media;

    @Column(nullable = false)
    private String userId;
}