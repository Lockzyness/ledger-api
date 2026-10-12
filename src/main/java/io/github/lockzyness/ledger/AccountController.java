package io.github.lockzyness.ledger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private AccountRepository accountRepository;
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public AccountController(AccountRepository accountRepository, UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody NewAccountRequest newAccountRequest, UriComponentsBuilder ucb){
        if(accountRepository.existsByOwner((newAccountRequest.owner()))){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Account already exists.");
        }
        Account newAcc = new Account(newAccountRequest.owner(), newAccountRequest.balance());
        Account savedAccount = accountRepository.save(newAcc);

        UserDetails newUser = User.builder()
                .username(newAccountRequest.owner())
                .password(passwordEncoder.encode(newAccountRequest.password()))
                .roles("OWNER")
                .build();
        userDetailsManager.createUser(newUser);

        URI locationOfNewAccount = ucb.path("/accounts/{id}").buildAndExpand(savedAccount.getId()).toUri();
        return ResponseEntity.created(locationOfNewAccount).build();
    };

    @GetMapping("/{requestedId}")
    public ResponseEntity<Account> getAccount(@PathVariable Long requestedId, Principal principal){
        //Optional<Account> accountOptional = accountRepository.findByIdAndOwner(requestedId, principal.getName());
        // why no need optional now?
        Account account = accountRepository.findByIdAndOwner(requestedId, principal.getName());

        // how will it return null auto?
        if(account == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }



}
