package com.rewards.api.service;

import com.rewards.api.dto.RewardResponse;
import com.rewards.api.model.Transaction;

import java.time.LocalDate;
import java.util.List;
/**
 * Service interface for managing rewards in the system.
 * Defines the contract for calculating reward points, saving transactions,
 * and retrieving reward summaries for customers.
 */
public interface RewardService {

    /**
     * Calculates reward points for a list of transactions.
     * @param transactions list of {@link Transaction} objects to process
     * @return a list of {@link RewardResponse} objects containing reward summaries
     */
    List<RewardResponse> calculateRewards(List<Transaction> transactions);

    /**
     * Saves a new transaction to the database.
     * @param request the {@link Transaction} object to be saved
     * @return the persisted {@link Transaction} with generated ID
     */
    Transaction saveTransaction(Transaction request);

    /**
     * Retrieves all transactions from the database and calculates rewards
     * for each customer.
     */
    List<RewardResponse> getAllRewards();

    /**
     * Retrieves all transactions for a specific customer and calculates
     * their total reward points.
     * @param customerId the unique identifier of the customer
     * @return a {@link RewardResponse} containing the reward summary
     *         (monthly points and total points) for the given customer
     */
    RewardResponse getRewardsByCustomerId(Long customerId);

    /**
     * Finds transactions between given date range provided by user
     */
    public List<RewardResponse> getRewardsByDateRange(LocalDate startDate, LocalDate endDate);
}
