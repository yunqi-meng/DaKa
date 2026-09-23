package com.daka.server.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "checkin_media")
public class CheckinMedia {
    @Id
    private String id;

    private String checkinId;
    private Integer mediaType;
    private String localPath;
    private String remoteUrl;
    private Long createTime = System.currentTimeMillis();
}