package com.daka.server.dto;

import lombok.Data;
import java.util.List;

@Data
public class CheckinDto {
    private String id;
    private Double latitude;
    private Double longitude;
    private String province;
    private String cityCode;
    private String city;
    private String district;
    private String address;
    private String title;
    private String content;
    private Integer unlockMode;
    private Integer revisitCount;
    private Boolean isForceUnlocked;
    private Long createTime;
    private Long updateTime;
    private List<MediaDto> media;
}