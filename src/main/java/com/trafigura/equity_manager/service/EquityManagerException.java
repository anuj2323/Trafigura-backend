package com.trafigura.equity_manager.service;

public class EquityManagerException extends RuntimeException {
    public EquityManagerException(String message) {
        super(message);
    }
    public EquityManagerException(String message, Throwable cause) {
        super(message, cause);
    }
} 