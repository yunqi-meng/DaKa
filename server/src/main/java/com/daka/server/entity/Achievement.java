package com.daka.server.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "achievement")
public class Achievement {
    @Id
    private String id;

    private String name;
    private String description;
    private String category;
    private Integer target;
    private Boolean isUnlocked = false;
    private Long unlockTime;
    private Integer progress = 0;

    @Column(nullable = false)
    private String userId;
}