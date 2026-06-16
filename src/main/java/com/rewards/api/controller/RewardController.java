package com.rewards.api.controller;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.service.RewardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rewards.api.model.Transaction;

import java.time.LocalDate;
import java.util.List;
/**
 * REST controller for managing rewards and transactions.
 * Provides endpoints to create transactions (bulk insert),
 * retrieve reward summaries for all customers, and fetch
 * rewards for a specific customer.
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;
    /**
     * Constructs a new {@code RewardController}.
     *
     * @param rewardService the service layer used to handle reward operations
     */
    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }
    /**
     * Creates multiple transactions in bulk.
     * @param requests list of {@link Transaction} objects to be created
     * @return a {@link ResponseEntity} containing the saved transactions
     *         and HTTP status {@code 201 CREATED}
     */
    @PostMapping("/transactions/bulk")
    public ResponseEntity<List<Transaction>> createTransactions(@Valid @RequestBody List<Transaction> requests) {
        List<Transaction> savedTransactions = requests.stream()
                .map(rewardService::saveTransaction)
                .toList();
        return new ResponseEntity<>(savedTransactions, HttpStatus.CREATED);
    }
    /**
     * Retrieves reward summaries for all customers.
     * @return a list of {@link RewardResponse} objects containing reward summaries
     *         for all customers
     */
    @GetMapping
    public List<RewardResponse> getRewards() {
        return rewardService.getAllRewards();
    }
    /**
     * Retrieves reward summary for a specific customer by their ID.
     * @param customerId the unique identifier of the customer
     * @return a {@link ResponseEntity} containing the reward summary
     *         for the specified customer
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<RewardResponse> getRewardsByCustomerId(@PathVariable
    @Positive(message="customerId must be positive")Long customerId) {
        RewardResponse rewards = rewardService.getRewardsByCustomerId(customerId);
        return ResponseEntity.ok(rewards);
    }
    /**
     * Retrieves reward details between any two specific date
     * Fetch rewards between a given date range
     */
    @GetMapping("/date-range")
    public List<RewardResponse> getRewardsByDateRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return rewardService.getRewardsByDateRange(startDate, endDate);
    }

}
