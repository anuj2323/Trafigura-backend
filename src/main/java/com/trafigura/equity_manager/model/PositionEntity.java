package com.trafigura.equity_manager.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "position")
public class PositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String securityCode;

    private int result;
} 