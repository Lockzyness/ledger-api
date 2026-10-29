package io.github.lockzyness.ledger;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page <Transaction> findByFromAccountOwnerOrToAccountOwner(String fromOwner, String toOwner, Pageable pageable);
    // Explicit query: a derived name like findByIdAndFromAccountOwnerOrToAccountOwner parses as
    // (id AND fromOwner) OR toOwner, which let recipients match transactions that aren't theirs.
    @Query("SELECT t FROM Transaction t WHERE t.id = :id AND (t.fromAccount.owner = :owner OR t.toAccount.owner = :owner)")
    Optional<Transaction> findByIdForOwner(@Param("id") Long id, @Param("owner") String owner);

    /*Transaction findByIdAndOwner(Long id, String fromAccount);
    Page<Transaction> findByOwner(String owner,PageRequest pageRequest);
    // List<Transaction> findByOwner(String owner);
    boolean existsByIdAndOwner(Long id, String owner);
    Transaction findById(long id);*/

}





