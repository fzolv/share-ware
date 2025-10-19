package com.fzolv.shareware.data.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
@Getter
@Setter
public class SettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "settlement_id")
    private UUID id;

    @Column(nullable = false)
    private UUID expenseId;

    @Column(nullable = false)
    private UUID fromUserId;

    @Column(nullable = false)
    private UUID toUserId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
