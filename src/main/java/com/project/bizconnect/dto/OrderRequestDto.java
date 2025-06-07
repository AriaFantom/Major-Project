package com.project.bizconnect.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequestDto> items;
}
