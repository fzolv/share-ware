package com.fzolv.shareware.balance.controllers;

import com.fzolv.shareware.balance.models.dtos.BalanceEntryDto;
import com.fzolv.shareware.balance.services.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<BalanceEntryDto>> getGroupBalance(@PathVariable("groupId") String groupId) {
        return ResponseEntity.ok(balanceService.getGroupBalanceSheet(groupId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BalanceEntryDto>> getUserBalance(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(balanceService.getUserBalanceSheet(userId));
    }
}
