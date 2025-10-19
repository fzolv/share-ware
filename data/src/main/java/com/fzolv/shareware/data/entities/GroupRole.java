package com.fzolv.shareware.data.entities;

import lombok.Getter;

@Getter
public enum GroupRole {
    GROUP_ADMIN(1),
    GROUP_MEMBER(5);

    final int priority;

    GroupRole(int p) {
        priority = p;
    }
}