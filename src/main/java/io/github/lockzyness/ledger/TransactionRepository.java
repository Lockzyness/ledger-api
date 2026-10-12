package io.github.lockzyness.ledger;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page <Transaction> findByFromAccountOwnerOrToAccountOwner(String fromOwner, String toOwner, Pageable pageable);
    Optional<Transaction> findByIdAndFromAccountOwnerOrToAccountOwner(Long id, String fromOwner, String toOwner);
    boolean existsByIdAndFromAccount_OwnerOrToAccount_Owner(Long id, String fromOwner, String toOwner);

    /*Transaction findByIdAndOwner(Long id, String fromAccount);
    Page<Transaction> findByOwner(String owner,PageRequest pageRequest);
    // List<Transaction> findByOwner(String owner);
    boolean existsByIdAndOwner(Long id, String owner);
    Transaction findById(long id);*/

}





