package com.trafigura.equity_manager.repo;

import com.trafigura.equity_manager.model.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepo extends JpaRepository<PositionEntity, Long> {
    PositionEntity findBySecurityCode(String securityCode);
} 