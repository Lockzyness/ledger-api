package io.github.lockzyness.ledger;

import jakarta.validation.constraints.NotBlank;

public record NewAccountRequest (@NotBlank String owner, @NotBlank String password){

}



