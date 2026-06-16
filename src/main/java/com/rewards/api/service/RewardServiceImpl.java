package com.rewards.api.service;

import com.rewards.api.Repository.TransactionRepository;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RewardService} that handles
 * reward calculation logic and transaction persistence.
 */
@Service
public class RewardServiceImpl implements RewardService {

    private final TransactionRepository transactionRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param transactionRepository repository to access transaction data
     */
    public RewardServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Saves a transaction to the database.
     *
     * @param request incoming transaction request
     * @return saved transaction entity
     */
    @Override
    public Transaction saveTransaction(Transaction request) {
        Transaction transaction = new Transaction();
        transaction.setCustomer(request.getCustomer());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());

        return transactionRepository.save(transaction);
    }

    /**
     * Calculates reward points for a list of transactions.
     * Groups transactions by customer and computes:
     * - Monthly reward points
     * - Total reward points
     *
     * @param transactions list of transactions
     * @return list of reward responses per customer
     */
    @Override
    public List<RewardResponse> calculateRewards(List<Transaction> transactions) {

        Map<Long, List<Transaction>> groupedTransactions =
                transactions.stream()
                        .collect(Collectors.groupingBy(t -> t.getCustomer().getId()));

        List<RewardResponse> responses = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry : groupedTransactions.entrySet()) {

            List<Transaction> customerTransactions = entry.getValue();

            Map<YearMonth, BigDecimal> monthlyPoints = new HashMap<>();
            BigDecimal totalPoints = BigDecimal.ZERO;

            for (Transaction transaction : customerTransactions) {

                BigDecimal points = calculatePoints(transaction.getAmount());
                YearMonth yearMonth = YearMonth.from(transaction.getTransactionDate());

                monthlyPoints.put(
                        yearMonth,
                        monthlyPoints.getOrDefault(yearMonth, BigDecimal.ZERO).add(points)
                );

                totalPoints = totalPoints.add(points);
            }

            Transaction firstTransaction = customerTransactions.get(0);

            responses.add(new RewardResponse(
                    firstTransaction.getCustomer().getId(),
                    firstTransaction.getCustomer().getName(),
                    monthlyPoints,
                    totalPoints,
                    customerTransactions
            ));
        }

        return responses;
    }

    /**
     * Calculates reward points based on transaction amount.
     * Rules:
     * - No points for amount <= 50
     * - 1 point per dollar for amount between 51–100
     * - 2 points per dollar above 100
     *
     * @param amount transaction amount
     * @return calculated reward points
     * @throws IllegalArgumentException if amount is negative
     */
    private BigDecimal calculatePoints(BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        BigDecimal points = BigDecimal.ZERO;

        if (amount.compareTo(BigDecimal.valueOf(100)) > 0) {
            points = points.add(
                    amount.subtract(BigDecimal.valueOf(100))
                            .multiply(BigDecimal.valueOf(2))
            );
            points = points.add(BigDecimal.valueOf(50));
        } else if (amount.compareTo(BigDecimal.valueOf(50)) > 0) {
            points = points.add(amount.subtract(BigDecimal.valueOf(50)));
        }

        return points;
    }

    /**
     * Retrieves rewards for all transactions.
     *
     * @return list of reward responses
     */
    @Override
    public List<RewardResponse> getAllRewards() {
        List<Transaction> transactions = transactionRepository.findAll();
        return calculateRewards(transactions);
    }

    /**
     * Retrieves reward details for a specific customer.
     *
     * @param customerId customer identifier
     * @return reward response
     * @throws RuntimeException if no transactions found
     */
    @Override
    public RewardResponse getRewardsByCustomerId(Long customerId) {

        List<Transaction> transactions =
                transactionRepository.findByCustomerId(customerId);

        if (transactions.isEmpty()) {
            throw new RuntimeException("No transactions found for customerId: " + customerId);
        }

        return calculateRewards(transactions).get(0);
    }

    /**
     * Retrieves rewards for transactions within a given date range.
     *
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of reward responses
     * @throws IllegalArgumentException if dates are invalid
     * @throws RuntimeException if no transactions found
     */
    @Override
    public List<RewardResponse> getRewardsByDateRange(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<Transaction> transactions =
                transactionRepository
                        .findByTransactionDateBetween(startDate, endDate);

        if (transactions.isEmpty()) {
            throw new RuntimeException("No transactions found between given dates");
        }

        return calculateRewards(transactions);
    }
}