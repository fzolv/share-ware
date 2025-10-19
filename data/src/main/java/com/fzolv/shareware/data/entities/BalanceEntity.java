package com.fzolv.shareware.data.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "balances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "from_user_id", "to_user_id"}))
@Getter
@Setter
public class BalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "balance_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private UserEntity fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false)
    private UserEntity toUser;

    @Column(nullable = false, precision = 5)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();


}
