package com.project.bizconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreVerificationRequest {
    private Long storeId;
    private boolean verified;
}
