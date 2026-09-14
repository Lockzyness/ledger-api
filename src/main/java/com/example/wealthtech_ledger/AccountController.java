package com.example.wealthtech_ledger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    public AccountController(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody NewAccountRequest newAccountRequest, UriComponentsBuilder ucb){
        if(accountRepository.existsByOwner((newAccountRequest.owner()))){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Account already exists.");
        }
        Account newAcc = new Account(newAccountRequest.owner(), newAccountRequest.balance());
        Account savedAccount = accountRepository.save(newAcc);
        URI locationOfNewAccount = ucb.path("/accounts/{id}").buildAndExpand(savedAccount.getId()).toUri();
        return ResponseEntity.created(locationOfNewAccount).build();
    };

}
