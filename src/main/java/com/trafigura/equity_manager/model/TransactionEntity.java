package com.trafigura.equity_manager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    private Integer tradeId;

    private Integer version;

    private String securityCode;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private Action action;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Enums
    public enum Action {
        INSERT, UPDATE, CANCEL
    }

    public enum Direction {
        BUY, SELL
    }
}
