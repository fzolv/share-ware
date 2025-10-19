package com.fzolv.shareware.hull.controllers;

import com.fzolv.shareware.hull.models.dtos.ExpenseDto;
import com.fzolv.shareware.hull.models.requests.ExpenseRequest;
import com.fzolv.shareware.hull.services.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #expenseRequest.groupId, 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<ExpenseDto> createExpense(@Valid @P("expenseRequest") @RequestBody ExpenseRequest expenseRequest) {
        return new ResponseEntity<>(expenseService.createExpense(expenseRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{expenseId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).EXPENSE, #expenseId, 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<ExpenseDto> updateExpense(@P("expenseId") @PathVariable("expenseId") String expenseId, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request));
    }

    @DeleteMapping("/{expenseId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).EXPENSE, #expenseId, 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteExpense(@P("expenseId") @PathVariable("expenseId") String expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{expenseId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).EXPENSE, #expenseId, 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<ExpenseDto> getExpense(@P("expenseId") @PathVariable("expenseId") String expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #groupId, 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<List<ExpenseDto>> getByGroup(@P("groupId") @PathVariable("groupId") String groupId) {
        return ResponseEntity.ok(expenseService.getExpensesByGroupId(groupId));
    }
}
