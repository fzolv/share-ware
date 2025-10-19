package com.fzolv.shareware.hull.controllers;

import com.fzolv.shareware.hull.models.requests.SettlementRequest;
import com.fzolv.shareware.hull.services.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/expense/{expenseId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).EXPENSE, #expenseId, 'MEMBER', 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<Void> settleExpense(@P("expenseId") @PathVariable("expenseId") String expenseId,
                                              @RequestBody SettlementRequest request) {
        settlementService.settleExpense(expenseId, request);
        return ResponseEntity.ok().build();
    }
}
