package com.rewards.api.service;

import com.rewards.api.dto.RewardResponse;
import com.rewards.api.model.Transaction;

import java.util.List;

public interface RewardService {

    List<RewardResponse> calculateRewards(List<Transaction> transactions);

    Transaction saveTransaction(Transaction request);

}