package com.project.bizconnect.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerStatsDto {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime joiningDate;
    private int totalOrders;
    private double totalSpent;
}
