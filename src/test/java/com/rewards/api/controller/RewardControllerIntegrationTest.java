package com.rewards.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rewards.api.Repository.TransactionRepository;
import com.rewards.api.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link RewardController}.
 * <p>
 * These tests validate the REST endpoints by simulating HTTP requests
 * and verifying responses. They ensure the controller, service, and repository
 * layers work together correctly.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class RewardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
    }

    /**
     * Test bulk creation of transactions via POST /api/rewards/transactions/bulk.
     * Ensures transactions are persisted and returned with HTTP 201 status.
     */
    @Test
    void testCreateTransactionsBulk() throws Exception {
        List<Transaction> transactions = List.of(
                new Transaction(1L, "Prerna", BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15)),
                new Transaction(1L, "Prerna", BigDecimal.valueOf(75), LocalDate.of(2024, 4, 10))
        );

        mockMvc.perform(post("/api/rewards/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].customerName").value("Prerna"))
                .andExpect(jsonPath("$[1].amount").value(75.0));
    }

    /**
     * Test fetching all rewards via GET /api/rewards.
     * Ensures reward summaries are returned for all customers.
     */
    @Test
    void testGetAllRewards() throws Exception {
        // Insert transactions first
        List<Transaction> transactions = List.of(
                new Transaction(1L, "Prerna", BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15)),
                new Transaction(1L, "Prerna", BigDecimal.valueOf(75), LocalDate.of(2024, 4, 10))
        );

        mockMvc.perform(post("/api/rewards/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isCreated());

        // Now fetch rewards
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Prerna"))
                .andExpect(jsonPath("$[0].totalPoints").value(115.0));
    }


    /**
     * Test fetching rewards for a specific customer via GET /api/rewards/{customerId}.
     * Ensures reward summary is returned with correct customer details.
     */
    @Test
    void testGetRewardsByCustomerId() throws Exception {
        // First insert transactions for customerId=2
        List<Transaction> transactions = List.of(
                new Transaction(2L, "Rahul", BigDecimal.valueOf(200), LocalDate.of(2024, 5, 5)),
                new Transaction(2L, "Rahul", BigDecimal.valueOf(45), LocalDate.of(2024, 6, 20))
        );

        mockMvc.perform(post("/api/rewards/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isCreated());

        // Then fetch rewards summary
        mockMvc.perform(get("/api/rewards/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(2))
                .andExpect(jsonPath("$.customerName").value("Rahul"))
                .andExpect(jsonPath("$.totalPoints").value(250.0));
    }
}
