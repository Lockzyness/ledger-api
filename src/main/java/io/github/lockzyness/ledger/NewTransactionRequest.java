package io.github.lockzyness.ledger;

import java.math.BigDecimal;

public record NewTransactionRequest(String description, BigDecimal amount, Long fromAccountId, Long toAccountId) {

}


