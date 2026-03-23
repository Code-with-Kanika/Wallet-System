package com.wallet.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {

    @Id
    private String txnId;

    private Long fromWallet;
    private Long toWallet;

    private BigDecimal amount;

    private String status; // SUCCESS / FAILED

    private LocalDateTime createdAt;
}