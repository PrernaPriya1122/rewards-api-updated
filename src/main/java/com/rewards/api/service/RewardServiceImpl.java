package com.rewards.api.service;
import com.rewards.api.Repository.CustomerRepository;
import com.rewards.api.Repository.TransactionRepository;
import com.rewards.api.dto.MonthlyReward;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.exception.CustomerNotFoundException;
import com.rewards.api.exception.TransactionNotFoundException;
import com.rewards.api.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RewardService} that handles
 * reward calculation logic and transaction persistence.
 */
@Service
public class RewardServiceImpl implements RewardService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    @Value("${rewards.months}")
    private int rewardMonths;

    /**
     * Constructor for dependency injection.
     *
     * @param transactionRepository repository to access transaction data
     */
    public RewardServiceImpl(TransactionRepository transactionRepository,CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.customerRepository=customerRepository;
    }

    /**
     * Retrieves rewards for all transactions for the configured period.
     *
     * @return list of reward responses
     */
    @Override
    public List<RewardResponse> getAllRewards() {

        LocalDate startDate = LocalDate.now().minusMonths(rewardMonths);

        List<Transaction> transactions = transactionRepository.findAll()
                .stream()
                .filter(transaction ->
                        !transaction.getTransactionDate().isBefore(startDate))
                .toList();

        return calculateRewards(transactions);
    }

    /**
     * Retrieves reward details for a specific customer.
     *
     * @param customerId customer identifier
     * @return reward response
     */
    @Override
    public RewardResponse getRewardsByCustomerId(Long customerId) {

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with ID: " + customerId));

        LocalDate startDate = LocalDate.now().minusMonths(rewardMonths);

        List<Transaction> transactions =
                transactionRepository.findByCustomerId(customerId)
                        .stream()
                        .filter(transaction ->
                                !transaction.getTransactionDate().isBefore(startDate))
                        .toList();

        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException(
                    "No transactions found for customer ID: " + customerId);
        }

        return calculateRewards(transactions).get(0);
    }

    /**
     * Retrieves rewards for transactions within a given date range.
     *
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of reward responses
     */
    @Override
    public List<RewardResponse> getRewardsByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must be before end date");
        }

        List<Transaction> transactions =
                transactionRepository.findByTransactionDateBetween(
                        startDate,
                        endDate);

        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException(
                    "No transactions found between given dates");
        }

        return calculateRewards(transactions);
    }

    /**
     * Calculates reward points for a list of transactions.
     * Groups transactions by customer and calculates:
     * <ul>
     *     <li>Monthly reward points</li>
     *     <li>Total reward points</li>
     * </ul>
     *
     * @param transactions list of transactions
     * @return list of reward responses
     */
    @Override
    public List<RewardResponse> calculateRewards(
            List<Transaction> transactions) {

        Map<Long, List<Transaction>> groupedTransactions =
                transactions.stream()
                        .collect(Collectors.groupingBy(
                                transaction ->
                                        transaction.getCustomer().getId()));

        List<RewardResponse> responses = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry
                : groupedTransactions.entrySet()) {

            List<Transaction> customerTransactions = entry.getValue();

            Map<YearMonth, BigDecimal> monthlyPoints = new HashMap<>();
            BigDecimal totalRewards = BigDecimal.ZERO;

            for (Transaction transaction : customerTransactions) {

                BigDecimal rewardPoints =
                        calculatePoints(transaction.getAmount());

                YearMonth yearMonth =
                        YearMonth.from(transaction.getTransactionDate());

                monthlyPoints.put(
                        yearMonth,
                        monthlyPoints.getOrDefault(
                                yearMonth,
                                BigDecimal.ZERO).add(rewardPoints)
                );

                totalRewards = totalRewards.add(rewardPoints);
            }

            List<MonthlyReward> monthlyRewards =
                    monthlyPoints.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(monthEntry ->
                                    new MonthlyReward(
                                            monthEntry.getKey().toString(),
                                            monthEntry.getValue()))
                            .toList();

            Transaction firstTransaction =
                    customerTransactions.get(0);

            responses.add(
                    new RewardResponse(
                            firstTransaction.getCustomer().getId(),
                            firstTransaction.getCustomer().getName(),
                            monthlyRewards,
                            totalRewards
                    )
            );
        }

        return responses;
    }

    /**
     * Calculates reward points based on transaction amount.
     *
     * Rules:
     * <ul>
     *     <li>No points for amount <= 50</li>
     *     <li>1 point per dollar spent between 51 and 100</li>
     *     <li>2 points per dollar spent above 100</li>
     * </ul>
     *
     * @param amount transaction amount
     * @return reward points
     */
    private BigDecimal calculatePoints(BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Amount cannot be negative");
        }

        BigDecimal points = BigDecimal.ZERO;

        if (amount.compareTo(BigDecimal.valueOf(100)) > 0) {

            points = points.add(
                    amount.subtract(BigDecimal.valueOf(100))
                            .multiply(BigDecimal.valueOf(2))
            );

            points = points.add(BigDecimal.valueOf(50));

        } else if (amount.compareTo(BigDecimal.valueOf(50)) > 0) {

            points = points.add(
                    amount.subtract(BigDecimal.valueOf(50))
            );
        }

        return points;
    }
}