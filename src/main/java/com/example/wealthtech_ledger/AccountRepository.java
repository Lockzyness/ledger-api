package com.example.wealthtech_ledger;

/*import org.hibernate.query.Page;*/ //  wrong page
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByIdAndOwner (Long id, String owner);
    Page<Account> findByOwner (String owner, PageRequest pageRequest);
    Account findById (long id);

    boolean existsByOwner(String owner);
}
