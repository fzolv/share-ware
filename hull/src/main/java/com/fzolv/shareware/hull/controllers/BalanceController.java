package com.fzolv.shareware.hull.controllers;

import com.fzolv.shareware.hull.models.dtos.BalanceEntryDto;
import com.fzolv.shareware.hull.services.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/group/{groupId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #groupId, 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<List<BalanceEntryDto>> getGroupBalance(@P("groupId") @PathVariable("groupId") String groupId) {
        return ResponseEntity.ok(balanceService.getGroupBalanceSheet(groupId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, #userId, 'MEMBER','ADMIN')")
    public ResponseEntity<List<BalanceEntryDto>> getUserBalance(@P("userId") @PathVariable("userId") String userId) {
        return ResponseEntity.ok(balanceService.getUserBalanceSheet(userId));
    }
}
