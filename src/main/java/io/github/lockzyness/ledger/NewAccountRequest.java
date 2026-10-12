package io.github.lockzyness.ledger;

import java.math.BigDecimal;

public record NewAccountRequest (String owner, String password, BigDecimal balance){

}



