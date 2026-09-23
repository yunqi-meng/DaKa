package com.daka.server.dto;

import lombok.Data;

@Data
public class MediaDto {
    private String id;
    private String checkinId;
    private Integer mediaType;
    private String remoteUrl;
}