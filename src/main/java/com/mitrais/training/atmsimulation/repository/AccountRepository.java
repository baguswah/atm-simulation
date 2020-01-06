package com.mitrais.training.atmsimulation.repository;

import org.springframework.data.repository.CrudRepository;
import com.mitrais.training.atmsimulation.model.Account;

public interface AccountRepository extends CrudRepository<Account, String> {
    Account findByAccountNumberAndPin(String accountNumber, String pin);
}
