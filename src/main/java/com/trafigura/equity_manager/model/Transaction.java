package com.trafigura.equity_manager.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Transaction {
    private String securityCode;
    private Integer tradeId;
    private Integer quantity;
    private Direction direction;
    private LocalDateTime createdAt;
    private Action action;

    public enum Action {
        INSERT, UPDATE, CANCEL
    }

    public enum Direction {
        BUY, SELL
    }
} 