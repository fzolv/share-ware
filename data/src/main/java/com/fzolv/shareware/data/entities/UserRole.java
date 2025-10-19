package com.fzolv.shareware.data.entities;

import lombok.Getter;

@Getter
public enum UserRole {

    ADMIN(0),
    MEMBER(10);

    final int priority;

    UserRole(int p) {
        priority = p;
    }
}


