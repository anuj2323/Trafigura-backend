package com.trafigura.equity_manager.repo;

import com.trafigura.equity_manager.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquityRepo extends JpaRepository<TransactionEntity, Long> {

    @Query("SELECT MAX(t.tradeId) FROM TransactionEntity t")
    Integer findMaxTradeId();

    boolean existsBySecurityCode(String securityCode);

    TransactionEntity findTopBySecurityCodeOrderByVersionDesc(String securityCode);

    List<TransactionEntity> findBySecurityCode(String securityCode);
}
