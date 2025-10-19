package com.fzolv.shareware.expense.controllers;

import com.fzolv.shareware.expense.models.dtos.ExpenseDto;
import com.fzolv.shareware.expense.models.requests.ExpenseRequest;
import com.fzolv.shareware.expense.services.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@Valid @RequestBody ExpenseRequest request) {
        return new ResponseEntity<>(expenseService.createExpense(request), HttpStatus.CREATED);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable("expenseId") String expenseId, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable("expenseId") String expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDto> getExpense(@PathVariable("expenseId") String expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseDto>> getByGroup(@PathVariable("groupId") String groupId) {
        return ResponseEntity.ok(expenseService.getExpensesByGroupId(groupId));
    }
}
