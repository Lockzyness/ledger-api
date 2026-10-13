package io.github.lockzyness.ledger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.data.domain.Pageable;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
class TransactionController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private TransactionController(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/{requestedId}")
    public ResponseEntity<Transaction> findById(@PathVariable Long requestedId, Principal principal){

        Optional<Transaction> transactionOptional = transactionRepository.findByIdForOwner(requestedId, principal.getName());
        if (transactionOptional.isPresent()){
            return ResponseEntity.ok(transactionOptional.get());
        }
        else {
            return ResponseEntity.notFound().build();
        }

        /*if (requestedId.equals(99L)){           replace hardcoded with .findById
            Transaction transaction = new Transaction(99L, "test get map", new BigDecimal("100"));
            return ResponseEntity.ok(transaction);
        }
        else {
            return ResponseEntity.notFound().build();
        }*/
    }

    @GetMapping             // return list
    private ResponseEntity<List<Transaction>> findAll(Pageable pageable, Principal principal){
        Page<Transaction> page = transactionRepository
                .findByFromAccountOwnerOrToAccountOwner(principal.getName(), principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
                ));
        return ResponseEntity.ok(page.getContent());
    }


    @PostMapping
    public ResponseEntity<Void> createTransaction(@RequestBody NewTransactionRequest newTransactionRequest, UriComponentsBuilder ucb, Principal principal){

        Account fromAccount = accountRepository.findById(newTransactionRequest.fromAccountId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"From account not found."));

        if (!fromAccount.getOwner().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this account.");
        }

        Account toAccount = accountRepository.findById(newTransactionRequest.toAccountId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"To account not found."));

        if (newTransactionRequest.amount().compareTo(fromAccount.getBalance()) > 0){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient funds");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(newTransactionRequest.amount()));
        toAccount.setBalance(toAccount.getBalance().add(newTransactionRequest.amount()));
        Transaction t = new Transaction(newTransactionRequest.description(), newTransactionRequest.amount(), fromAccount, toAccount);
        Transaction savedTransaction = transactionRepository.save(t);
        URI locationOfNewTransaction = ucb.path("/transactions/{id}").buildAndExpand(savedTransaction.getId()).toUri();
        return ResponseEntity.created(locationOfNewTransaction).build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteTransaction(@PathVariable Long id, Principal principal){

        if(!transactionRepository.existsByIdAndFromAccount_OwnerOrToAccount_Owner(id, principal.getName(), principal.getName())){
            return ResponseEntity.notFound().build();
        }

        transactionRepository.deleteById(id);
        return ResponseEntity.noContent().build();

        // equivalent if dont want ! operator. return will terminate the code.
        /*if (cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();*/
    }
}
