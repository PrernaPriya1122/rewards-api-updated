package com.rewards.api.controller;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.service.RewardService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
@Validated
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
     * Retrieves reward summaries for all customers.
     * @return a list of {@link RewardResponse} objects containing reward summaries
     *         for all customers
     */
    @GetMapping("/all")
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
    @Positive(message="Customer ID must be greater than zero")Long customerId) {
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
