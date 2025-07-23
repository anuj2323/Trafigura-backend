package com.trafigura.equity_manager.model;

import lombok.Data;

@Data
public class PositionDto {
    private String securityCode;
    private int result;
} 