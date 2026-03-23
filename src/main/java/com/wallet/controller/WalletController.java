package com.wallet.controller;

import com.wallet.entity.Transaction;
import com.wallet.entity.Wallet;
import com.wallet.service.WalletService;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/create")
    public Wallet createWallet(@RequestParam Long userId) {
        return walletService.createWallet(userId);
    }

    @GetMapping("/add-money")
    public Wallet addMoney(@RequestParam Long walletId , @RequestParam BigDecimal amount){
        return walletService.addMoney(walletId,amount);
    }

    @GetMapping("/transfer")
    public String transfer(
            @RequestHeader("Idempotency-Key") String key,
            @RequestParam Long fromWallet,
            @RequestParam Long toWallet,
            @RequestParam BigDecimal amount) {

        return walletService.transfer(fromWallet, toWallet, amount, key);
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(@RequestParam Long walletId) {
        return walletService.getTransactions(walletId);
    }
}