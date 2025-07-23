package com.trafigura.equity_manager.controller;

import com.trafigura.equity_manager.model.PositionDto;
import com.trafigura.equity_manager.model.Transaction;
import com.trafigura.equity_manager.model.TransactionEntity;
import com.trafigura.equity_manager.service.EquityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equity/")
public class EquityController {
    private final EquityService equityService;

    public EquityController(EquityService equityService) {
        this.equityService = equityService;
    }
    @PostMapping("save")
    public ResponseEntity<Transaction> save(@RequestBody Transaction transaction) {
        Transaction saved = equityService.save(transaction);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("transactions")
    public ResponseEntity<List<TransactionEntity>> getAllTransactions() {
        List<TransactionEntity> transactions = equityService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("position/{securityCode}")
    public ResponseEntity<PositionDto> getPosition(@PathVariable String securityCode) {
        PositionDto position = equityService.getPosition(securityCode);
        return ResponseEntity.ok(position);
    }

    @GetMapping("positions")
    public ResponseEntity<List<PositionDto>> getAllPositions() {
        List<PositionDto> positions = equityService.getAllPositions();
        return ResponseEntity.ok(positions);
    }
}
