package com.rewards.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
/**
 * DTO representing reward points earned by a customer
 * for a specific month.
 *
 * <p>This class is used as part of the reward summary
 * response to provide a month-wise breakdown of reward
 * points earned by a customer.</p>
 */
@Getter
@AllArgsConstructor
public class MonthlyReward {

    /**
     * Month for which reward points were calculated.
     * Format: yyyy-MM (for example, 2026-04).
     */
    private String month;

    /**
     * Total reward points earned in the specified month.
     */
    private BigDecimal rewardPoints;
}