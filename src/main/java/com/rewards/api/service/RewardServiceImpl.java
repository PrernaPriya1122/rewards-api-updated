package com.rewards.api.service;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.model.Transaction;
import com.rewards.api.Repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Implementation of the RewardService interface.
 * Provides methods to save transactions, fetch customer details,
 * and calculate reward points based on transaction history.
 */
@Service
public class RewardServiceImpl implements RewardService {

    private final TransactionRepository transactionRepository;
    /**
     * Constructor injection for TransactionRepository.
     *
     * @param transactionRepository repository for accessing transaction data
     */
    public RewardServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    /**
     * Saves a new transaction to the database.
     *
     * @param request transaction request object
     * @return saved Transaction entity
     */
    @Override
    public Transaction saveTransaction(Transaction request) {
        Transaction transaction = new Transaction();
        transaction.setCustomerId(request.getCustomerId());
        transaction.setCustomerName(request.getCustomerName());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        return transactionRepository.save(transaction);
    }
    /**
     * Calculates reward points for each customer grouped by YearMonth.
     *
     * @param transactions list of transactions
     * @return list of RewardResponse containing monthly and total points
     */
    @Override
    public List<RewardResponse> calculateRewards(List<Transaction> transactions) {
        Map<Long, List<Transaction>> groupedTransactions =
                transactions.stream().collect(Collectors.groupingBy(Transaction::getCustomerId));

        List<RewardResponse> responses = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry : groupedTransactions.entrySet()) {
            List<Transaction> customerTransactions = entry.getValue();

            Map<YearMonth, BigDecimal> monthlyPoints = new HashMap<>();
            BigDecimal totalPoints = BigDecimal.ZERO;

            for (Transaction transaction : customerTransactions) {
                BigDecimal points = calculatePoints(transaction.getAmount());
                YearMonth yearMonth = YearMonth.from(transaction.getTransactionDate());

                monthlyPoints.put(yearMonth,
                        monthlyPoints.getOrDefault(yearMonth, BigDecimal.ZERO).add(points));

                totalPoints = totalPoints.add(points);
            }

            Transaction firstTransaction = customerTransactions.get(0);

            responses.add(new RewardResponse(
                    firstTransaction.getCustomerId(),
                    firstTransaction.getCustomerName(),
                    monthlyPoints,
                    totalPoints, customerTransactions
            ));
        }

        return responses;
    }
    /**
     * Reward Calculation Rules:
     * - 2 points for every dollar above $100
     * - 1 point for every dollar between $50 and $100
     *
     * @param amount transaction amount
     * @return reward points as BigDecimal
     * @throws IllegalArgumentException if amount is negative
     */
    private BigDecimal calculatePoints(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        BigDecimal points = BigDecimal.ZERO;

        if (amount.compareTo(BigDecimal.valueOf(100)) > 0) {
            points = points.add(amount.subtract(BigDecimal.valueOf(100)).multiply(BigDecimal.valueOf(2)));
            points = points.add(BigDecimal.valueOf(50)); // for 50–100 range
        } else if (amount.compareTo(BigDecimal.valueOf(50)) > 0) {
            points = points.add(amount.subtract(BigDecimal.valueOf(50)));
        }

        return points;
    }
    /**
     * Retrieves all transactions from the database and calculates rewards
     * for each customer.
     * @return a list of {@link RewardResponse} objects containing reward summaries
     *         (monthly points, total points, and raw transactions) for all customers
     */
    @Override
    public List<RewardResponse> getAllRewards() {
        List<Transaction> transactions = transactionRepository.findAll();
        return calculateRewards(transactions);
    }
    /**
     * Retrieves all transactions for a specific customer and calculates
     * their reward points.
     * @param customerId the unique identifier of the customer
     * @return a {@link RewardResponse} containing the reward summary
     *         (monthly points, total points, and raw transactions) for the given customer
     * @throws RuntimeException if no transactions are found for the specified customerId
     */
    @Override
    public RewardResponse getRewardsByCustomerId(Long customerId) {
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        if (transactions.isEmpty()) {
            throw new RuntimeException("No transactions found for customerId: " + customerId);
        }
        return calculateRewards(transactions).get(0); // only one customer group
    }

}
