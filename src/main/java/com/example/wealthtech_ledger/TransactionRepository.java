package com.example.wealthtech_ledger;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

interface TransactionRepository extends CrudRepository<Transaction, Long>, PagingAndSortingRepository<Transaction, Long> {

    Transaction findByIdAndOwner(Long id, String owner);
    Page<Transaction> findByOwner(String owner,PageRequest pageRequest);
    boolean existsByIdAndOwner(Long id, String owner);

}





