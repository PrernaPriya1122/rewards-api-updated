package com.rewards.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rewards.api.Repository.TransactionRepository;
import com.rewards.api.model.Customer;
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
 * These tests validate REST APIs end-to-end including controller,
 * service, and repository layers using MockMvc.
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
     * Utility method to create transaction with Customer object.
     */
    private Transaction createTransaction(Long id, String name,
                                          BigDecimal amount, LocalDate date) {
        Customer customer = new Customer(id, name);

        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(amount);
        transaction.setTransactionDate(date);

        return transaction;
    }

    /**
     * Test bulk transaction creation.
     *
     * Verifies:
     * - HTTP 201 status
     * - Transactions are returned correctly
     */
    @Test
    void testCreateTransactionsBulk() throws Exception {

        List<Transaction> transactions = List.of(
                createTransaction(1L, "Prerna", BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15)),
                createTransaction(1L, "Prerna", BigDecimal.valueOf(75), LocalDate.of(2024, 4, 10))
        );

        mockMvc.perform(post("/api/rewards/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].customer.name").value("Prerna"))
                .andExpect(jsonPath("$[1].amount").value(75.0));
    }

    /**
     * Test fetching all rewards.
     *
     * Verifies:
     * - Rewards are calculated correctly
     * - Customer details are correct
     */
    @Test
    void testGetAllRewards() throws Exception {

        List<Transaction> transactions = List.of(
                createTransaction(1L, "Prerna", BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15)),
                createTransaction(1L, "Prerna", BigDecimal.valueOf(75), LocalDate.of(2024, 4, 10))
        );

        mockMvc.perform(post("/api/rewards/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Prerna"))
                .andExpect(jsonPath("$[0].totalPoints").value(115.0));
    }

    /**
     * Test fetching rewards by customerId.
     *
     * Verifies:
     * - Correct customer details
     * - Correct reward calculation
     */
    @Test
    void testGetRewardsByCustomerId() throws Exception {

        List<Transaction> transactions = List.of(
                createTransaction(2L, "Rahul", BigDecimal.valueOf(200), LocalDate.of(2024, 5, 5)),
                createTransaction(2L, "Rahul", BigDecimal.valueOf(45), LocalDate.of(2024, 6, 20))
        );

        mockMvc.perform(post("/api/rewards/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/rewards/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(2))
                .andExpect(jsonPath("$.customerName").value("Rahul"))
                .andExpect(jsonPath("$.totalPoints").value(250.0));
    }

    /**
     * Test fetching rewards within a date range.
     *
     * Verifies:
     * - Only transactions within range are included
     * - Rewards are calculated correctly
     */
    @Test
    void testGetRewardsByDateRange() throws Exception {

        List<Transaction> transactions = List.of(
                createTransaction(1L, "Prerna", BigDecimal.valueOf(120), LocalDate.of(2024, 1, 10)),
                createTransaction(1L, "Prerna", BigDecimal.valueOf(75), LocalDate.of(2024, 3, 10))
        );

        mockMvc.perform(post("/api/rewards/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/rewards/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].totalPoints").value(90.0));
    }

    /**
     * Test date range API with no data.
     *
     * Verifies:
     * - Proper exception handling (404 or error response)
     */
    @Test
    void testGetRewardsByDateRangeNoData() throws Exception {

        mockMvc.perform(get("/api/rewards/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isNotFound());
    }
}
