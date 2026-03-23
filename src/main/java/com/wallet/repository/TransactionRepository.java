package com.wallet.repository;

import com.wallet.entity.Transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByFromWalletOrToWalletOrderByCreatedAtDesc(Long fromWallet, Long toWallet);
}