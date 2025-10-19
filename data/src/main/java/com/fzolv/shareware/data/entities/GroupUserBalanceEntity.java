package com.fzolv.shareware.data.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "group_user_balances",
    uniqueConstraints = @UniqueConstraint(columnNames = {"balance_id", "borrower_id", "lender_id"})
)
@Getter
@Setter
public class GroupUserBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "balance_id", nullable = false)
    private GroupBalanceEntity balance;

    @Column(name = "borrower_id", nullable = false)
    private UUID borrowerId;

    @Column(name = "lender_id", nullable = false)
    private UUID lenderId;

    @Column(name = "amount", nullable = false, precision = 10)
    private Double amount;
}


