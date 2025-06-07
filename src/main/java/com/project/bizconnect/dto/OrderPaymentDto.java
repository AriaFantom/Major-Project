package com.project.bizconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderPaymentDto {
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotBlank(message = "Payment ID is required")
    private String paymentId;
}
