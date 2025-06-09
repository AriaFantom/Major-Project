package com.project.bizconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowerDto {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime followedAt;
}
