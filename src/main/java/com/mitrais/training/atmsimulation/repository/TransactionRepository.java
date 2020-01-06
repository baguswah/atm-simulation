package com.mitrais.training.atmsimulation.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import com.mitrais.training.atmsimulation.model.Transaction;

public interface TransactionRepository extends PagingAndSortingRepository<Transaction, Integer> {

}
