package com.rewards.api.dto;
import com.rewards.api.model.Transaction;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
/**
 * DTO (Data Transfer Object) representing the reward summary for a customer.
 * This class aggregates reward points earned by a customer across multiple transactions.
 * It provides a breakdown of points per month, the total points accumulated,
 * and the list of raw transactions used to calculate the rewards
 */
@Getter
public class RewardResponse {

    /** Unique identifier of the customer. */
    private Long customerId;

    /** Name of the customer. */
    private String customerName;

    /** Reward points earned per month */
    private Map<YearMonth, BigDecimal> monthlyPoints;

    /** Total reward points accumulated across all transactions. */
    private BigDecimal totalPoints;

    /** Raw list of transactions for the customer. */
    private List<Transaction> transactions;

    /**
     * Constructs a new {@code RewardResponse}.
     *
     * @param customerId    the unique identifier of the customer
     * @param customerName  the name of the customer
     * @param monthlyPoints map of {@link YearMonth} to reward points earned in that month
     * @param totalPoints   total reward points accumulated across all transactions
     * @param transactions  list of raw {@link Transaction} objects for the customer
     */
    public RewardResponse(Long customerId,
                          String customerName,
                          Map<YearMonth, BigDecimal> monthlyPoints,
                          BigDecimal totalPoints,
                          List<Transaction> transactions) {

        this.customerId = customerId;
        this.customerName = customerName;
        this.monthlyPoints = monthlyPoints;
        this.totalPoints = totalPoints;
        this.transactions = transactions;
    }
}
