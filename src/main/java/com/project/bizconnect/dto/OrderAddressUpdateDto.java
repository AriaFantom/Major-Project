package com.project.bizconnect.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderAddressUpdateDto {
    @NotNull(message = "Address ID is required")
    private Integer addressId;
}
