package com.wallet.repository;

import com.wallet.entity.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<Idempotency, String> {
}