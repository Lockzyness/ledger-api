package io.github.lockzyness.ledger;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record NewTransactionRequest(String description,
                        @NotNull @Positive BigDecimal amount,
                        @NotNull @Positive Long fromAccountId,
                        @NotNull @Positive Long toAccountId) {

}


