package com.mitrais.training.atmsimulation.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.mitrais.training.atmsimulation.model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Integer> {
    @Query("select t from Transaction t where t.accountNumber = :account")
    public Page<Transaction> findTransactionByAccountNumber(@Param("account") String accountNumber, Pageable pageable);

    @Query("select t from Transaction t where t.accountNumber = :account and t.transactionDate >= :startDate and t.transactionDate <= :endDate")
    public Page<Transaction> findTransactionByAccountOnDate(@Param("account") String accountNumber,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);
}
