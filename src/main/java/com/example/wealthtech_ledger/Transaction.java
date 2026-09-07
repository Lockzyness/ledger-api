package com.example.wealthtech_ledger;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

record Transaction (@Id Long id, String description, BigDecimal amount, String owner){


}
