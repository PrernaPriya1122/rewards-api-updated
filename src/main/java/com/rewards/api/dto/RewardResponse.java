package com.rewards.api.dto;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO (Data Transfer Object) representing the reward summary for a customer.
 * This class aggregates reward points earned by a customer across multiple transactions.
 * It provides a breakdown of points per month, the total points accumulated,
 * and the list of raw transactions used to calculate the rewards
 */
@Getter
public class RewardResponse {

    private Long customerId;

    private String customerName;

    private List<MonthlyReward> monthlyRewards;

    private BigDecimal totalRewards;

    public RewardResponse(Long customerId,
                          String customerName,
                          List<MonthlyReward> monthlyRewards,
                          BigDecimal totalRewards) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.monthlyRewards = monthlyRewards;
        this.totalRewards = totalRewards;
    }
}
