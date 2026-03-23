package com.wallet.entity;

import com.wallet.enums.IdempotencyStatus;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Idempotency {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;

    @Lob
    private String response;
    
}