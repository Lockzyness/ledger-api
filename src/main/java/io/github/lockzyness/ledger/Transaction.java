package io.github.lockzyness.ledger;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "FROM_ACCOUNT")
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "TO_ACCOUNT")
    private Account toAccount;

    protected Transaction(){}

    public Transaction(String description, BigDecimal amount, Account fromAccount, Account toAccount) {
        this.description = description;
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    @Override
    public String toString(){
        return "Transaction[id: " + id + ", description: " + description + ", amount: " + amount + ", from: " + fromAccount + ", to: " + toAccount;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public Boolean checkSufficientFund(BigDecimal amount){
        BigDecimal bal = fromAccount.getBalance();
        return amount.compareTo(bal) <= 0;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }
}
