package com.rewards.api.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class RewardResponse {

    private Long customerId;
    private String customerName;
    private Map<String, Integer> monthlyPoints;
    private int totalPoints;

    public RewardResponse(Long customerId,
                          String customerName,
                          Map<String, Integer> monthlyPoints,
                          int totalPoints) {

        this.customerId = customerId;
        this.customerName = customerName;
        this.monthlyPoints = monthlyPoints;
        this.totalPoints = totalPoints;
    }

}
