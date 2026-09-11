package com.example.wealthtech_ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findByIdAndOwner(Long id, String owner);
    Page<Transaction> findByOwner(String owner,PageRequest pageRequest);
    // List<Transaction> findByOwner(String owner);
    boolean existsByIdAndOwner(Long id, String owner);
    Transaction findById(long id);

}





