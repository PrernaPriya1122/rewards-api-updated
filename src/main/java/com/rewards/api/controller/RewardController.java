package com.rewards.api.controller;

import com.rewards.api.dto.RewardResponse;
import com.rewards.api.service.RewardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rewards.api.model.Transaction;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(
            RewardService rewardService) {

        this.rewardService = rewardService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction request) {

        Transaction savedTransaction = rewardService.saveTransaction(request);

        return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);
    }

    @GetMapping
    public List<RewardResponse> getRewards() {
        List<Transaction> transactions=new ArrayList<>();
        return rewardService.calculateRewards(transactions);
    }
}