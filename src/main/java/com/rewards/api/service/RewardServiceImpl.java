package com.rewards.api.service;

import com.rewards.api.dto.RewardResponse;
import com.rewards.api.model.Transaction;
import com.rewards.api.Repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RewardServiceImpl implements RewardService {

    private final TransactionRepository transactionRepository;

    public RewardServiceImpl(TransactionRepository transactionRepository) {

        this.transactionRepository = transactionRepository;
    }
    @Override
    public Transaction saveTransaction(Transaction request) {

        Transaction transaction = new Transaction();

        transaction.setCustomerId(request.getCustomerId());

        transaction.setCustomerName(request.getCustomerName());

        transaction.setAmount(request.getAmount());

        transaction.setTransactionDate(request.getTransactionDate());

        return transactionRepository.save(transaction);
    }
    @Override
    public List<RewardResponse> calculateRewards(List<Transaction> transactions) {

        Map<Long, List<Transaction>> groupedTransactions =transactions.stream()
                        .collect(Collectors.groupingBy(Transaction::getCustomerId));

        List<RewardResponse> responses = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry : groupedTransactions.entrySet()) {

            List<Transaction> customerTransactions = entry.getValue();

            Map<String, Integer> monthlyPoints = new HashMap<>();
            int totalPoints = 0;

            for (Transaction transaction : customerTransactions) {

                int points = calculatePoints(transaction.getAmount());

                Month month = transaction.getTransactionDate().getMonth();

                monthlyPoints.put(month.name(),
                        monthlyPoints.getOrDefault(month.name(), 0) + points
                );

                totalPoints += points;
            }

            Transaction firstTransaction = customerTransactions.get(0);

            responses.add(
                    new RewardResponse(
                            firstTransaction.getCustomerId(),
                            firstTransaction.getCustomerName(),
                            monthlyPoints,
                            totalPoints
                    )
            );
        }

        return responses;
    }

    /**
     * Reward Calculation Rules:
     * 2 points for every dollar above $100
     * 1 point for every dollar between $50 and $100
     */
    private int calculatePoints(BigDecimal amount) {

        int value = amount.intValue();

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        if (value <= 50) {
            return 0;
        }

        if (value <= 100) {
            return value - 50;
        }

        return ((value - 100) * 2) + 50;
    }
}