package com.fzolv.shareware.expense.models.requests;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class ExpenseRequest {
    @NotBlank
    private String groupId;

    @NotNull
    private Double amount;

    private String description;

    @NotBlank
    private String paidById;

    private String currency = "INR";

    @NotBlank
    private String splitType; // EQUAL, EXACT, PERCENTAGE

    private List<SplitRequest> splits;

    @Data
    public static class SplitRequest {
        private String userId;
        private Double amount;
        private Double percentage;
    }
}
