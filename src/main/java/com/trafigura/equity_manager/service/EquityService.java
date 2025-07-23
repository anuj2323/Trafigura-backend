package com.trafigura.equity_manager.service;

import com.trafigura.equity_manager.model.Transaction;
import com.trafigura.equity_manager.model.TransactionEntity;
import com.trafigura.equity_manager.model.PositionDto;
import com.trafigura.equity_manager.model.PositionEntity;
import com.trafigura.equity_manager.repo.EquityRepo;
import com.trafigura.equity_manager.repo.PositionRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EquityService {

    private static final Logger logger = LoggerFactory.getLogger(EquityService.class);

    private final EquityRepo equityRepo;
    private final PositionRepo positionRepo;

    public EquityService(EquityRepo equityRepo, PositionRepo positionRepo) {
        this.equityRepo = equityRepo;
        this.positionRepo = positionRepo;
    }

    @Transactional
    public Transaction save(Transaction transaction) {
        try {
            TransactionEntity entity;
            TransactionEntity latestInserted = equityRepo.findTopByOrderByTransactionIdDesc();

            boolean isNewTrade = latestInserted != null &&
                    "CANCEL".equalsIgnoreCase(String.valueOf(latestInserted.getAction()));

            if(!equityRepo.existsBySecurityCode(transaction.getSecurityCode()) || isNewTrade) {
                int tradeId = getNextTradeId();
                transaction.setTradeId(tradeId);
                entity = mapToEntity(transaction, 1);
                equityRepo.save(entity);
            } else {
                TransactionEntity latest = equityRepo.findTopBySecurityCodeOrderByVersionDesc(transaction.getSecurityCode());

                int tradeId = latest.getTradeId();
                int newVersion = (latest.getVersion() != null) ? latest.getVersion() + 1 : 1;

                entity = mapToEntity(transaction, newVersion);
                entity.setTradeId(tradeId);
                entity.setTransactionId(null);
                equityRepo.save(entity);
            }
            // Update position after saving transaction
            updatePositionForSecurity(transaction.getSecurityCode());
            return mapToTransaction(entity);
        } catch (Exception e) {
            logger.error("Error saving transaction or updating position for securityCode: {}", transaction.getSecurityCode(), e);
            throw new EquityManagerException("Failed to save transaction or update position", e);
        }
    }

    public List<TransactionEntity> getAllTransactions() {
        return equityRepo.findAll();
    }

    public PositionDto getPosition(String securityCode) {
        List<TransactionEntity> records = equityRepo.findBySecurityCode(securityCode);
        PositionDto dto = new PositionDto();
        dto.setSecurityCode(securityCode);
        boolean hasCancel = records.stream().anyMatch(r -> r.getAction() == TransactionEntity.Action.CANCEL);
        if (hasCancel) {
            dto.setResult(0);
        } else {
            int buy = getLatestQuantity(records, TransactionEntity.Direction.BUY);
            int sell = getLatestQuantity(records, TransactionEntity.Direction.SELL);
            dto.setResult(buy - sell);
        }
        return dto;
    }

    public List<PositionDto> getAllPositions() {
        // Return all data from the position table
        List<PositionEntity> all = positionRepo.findAll();
        return all.stream().map(entity -> {
            PositionDto dto = new PositionDto();
            dto.setSecurityCode(entity.getSecurityCode());
            dto.setResult(entity.getResult());
            return dto;
        }).toList();
    }

    private int getNextTradeId() {
        Integer max = equityRepo.findMaxTradeId();
        return (max != null) ? max + 1 : 1;
    }

    private TransactionEntity mapToEntity(Transaction transaction, int version) {
        TransactionEntity entity = new TransactionEntity();
        entity.setTradeId(transaction.getTradeId());
        entity.setVersion(version);
        entity.setSecurityCode(transaction.getSecurityCode());
        entity.setQuantity(transaction.getQuantity());
        if (transaction.getAction() != null) {
            entity.setAction(TransactionEntity.Action.valueOf(transaction.getAction().name()));
        }
        if (transaction.getDirection() != null) {
            entity.setDirection(TransactionEntity.Direction.valueOf(transaction.getDirection().name()));
        }
        entity.setCreatedAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt() : java.time.LocalDateTime.now());
        return entity;
    }

    private Transaction mapToTransaction(TransactionEntity entity) {
        Transaction transaction = new Transaction();
        transaction.setSecurityCode(entity.getSecurityCode());
        transaction.setTradeId(entity.getTradeId());
        transaction.setQuantity(entity.getQuantity());
        transaction.setDirection(entity.getDirection() != null ? Transaction.Direction.valueOf(entity.getDirection().name()) : null);
        transaction.setCreatedAt(entity.getCreatedAt());
        transaction.setAction(entity.getAction() != null ? Transaction.Action.valueOf(entity.getAction().name()) : null);
        return transaction;
    }

    private int getLatestQuantity(List<TransactionEntity> records, TransactionEntity.Direction direction) {
        return records.stream()
            .filter(r -> r.getDirection() == direction && r.getAction() == TransactionEntity.Action.UPDATE)
            .max(Comparator.comparingInt(TransactionEntity::getVersion))
            .map(TransactionEntity::getQuantity)
            .orElseGet(() ->
                records.stream()
                    .filter(r -> r.getDirection() == direction && r.getAction() == TransactionEntity.Action.INSERT)
                    .max(Comparator.comparingInt(TransactionEntity::getVersion))
                    .map(TransactionEntity::getQuantity)
                    .orElse(0)
            );
    }

    private void updatePositionForSecurity(String securityCode) {
        try {
            PositionDto dto = getPosition(securityCode);
            PositionEntity position = positionRepo.findBySecurityCode(securityCode);
            if (position == null) {
                position = new PositionEntity();
                position.setSecurityCode(securityCode);
            }
            position.setResult(dto.getResult());
            positionRepo.save(position);
        } catch (Exception e) {
            logger.error("Error updating position for securityCode: {}", securityCode, e);
            throw new EquityManagerException("Failed to update position for securityCode: " + securityCode, e);
        }
    }
}
