package com.example.wealthtech_ledger;

import java.math.BigDecimal;

public record NewTransactionRequest(String description, BigDecimal amount, Long fromAccountId, Long toAccountId) {
    @Override
    public String description() {
        return description;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }

    @Override
    public Long fromAccountId() {
        return fromAccountId;
    }

    @Override
    public Long toAccountId() {
        return toAccountId;
    }
}


