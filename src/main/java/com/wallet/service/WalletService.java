package com.wallet.service;

import com.wallet.entity.Idempotency;
import com.wallet.entity.Transaction;
import com.wallet.entity.Wallet;
import com.wallet.enums.IdempotencyStatus;
import com.wallet.repository.IdempotencyRepository;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Wallet createWallet(Long userId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);

        return walletRepository.save(wallet);
    }
    /*//Without concurrency
    public Wallet addMoney(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found!!"));

        wallet.setBalance( wallet.getBalance().add(amount));
        return walletRepository.save(wallet);

    }*/ 

    public Wallet addMoney(Long walletId, BigDecimal amount) {
        int retries=3;
        while(retries >0 ){
            try{
                Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found!!"));

                wallet.setBalance( wallet.getBalance().add(amount));
                return walletRepository.save(wallet);
            }catch(ObjectOptimisticLockingFailureException e){
                 retries--;
                if (retries == 0) {
                    throw new RuntimeException("Failed due to concurrent update. Please try again.");
                }
            }

        }
        throw new RuntimeException("Unexpected error");
        
    }

    @Transactional
    public String transfer(Long fromId, Long toId, BigDecimal amount, String key) {

        // Step 1: Check if already exists
        Optional<Idempotency> existing = idempotencyRepository.findById(key);

        if (existing.isPresent()) {
            Idempotency id = existing.get();

            if (id.getStatus() == IdempotencyStatus.SUCCESS) {
                return id.getResponse();
            }

            if (id.getStatus() == IdempotencyStatus.PENDING) {
                throw new RuntimeException("Request is already in progress");
            }
        }

        // Step 2: Insert PENDING first
        Idempotency id = new Idempotency();
        id.setId(key);
        id.setStatus(IdempotencyStatus.PENDING);
        idempotencyRepository.save(id);

        int retries = 3;

        while (retries > 0) {
            try {
                Wallet sender = walletRepository.findById(fromId).orElseThrow();
                Wallet receiver = walletRepository.findById(toId).orElseThrow();

                if (sender.getBalance().compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient balance");
                }

                sender.setBalance(sender.getBalance().subtract(amount));
                receiver.setBalance(receiver.getBalance().add(amount));

                walletRepository.save(sender);
                walletRepository.save(receiver);

                // Ledger
                String txnId = UUID.randomUUID().toString();

                Transaction txn = new Transaction();
                txn.setTxnId(txnId);
                txn.setFromWallet(fromId);
                txn.setToWallet(toId);
                txn.setAmount(amount);
                txn.setStatus("SUCCESS");
                txn.setCreatedAt(LocalDateTime.now());

                transactionRepository.save(txn);

                String response = "TxnId: " + txnId;

                // ✅ Step 3: Mark SUCCESS
                id.setStatus(IdempotencyStatus.SUCCESS);
                id.setResponse(response);
                idempotencyRepository.save(id);

                return response;

            } catch (ObjectOptimisticLockingFailureException e) {
                retries--;
                if (retries == 0) {
                    throw new RuntimeException("Failed due to concurrency");
                }
            }
        }

        throw new RuntimeException("Unexpected error");
    }

    public List<Transaction> getTransactions(Long walletId) {
        return transactionRepository.findByFromWalletOrToWalletOrderByCreatedAtDesc(walletId, walletId);
    }
}