package com.rewards.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rewards.api.Repository.CustomerRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RewardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        customerRepository.deleteAll();
    }

    private Transaction createTransaction(Customer customer,
                                          BigDecimal amount,
                                          LocalDate date) {

        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(amount);
        transaction.setTransactionDate(date);

        return transaction;
    }

    @Test
    void testCreateTransactionsBulk() throws Exception {

        Customer customer = customerRepository.save(
                new Customer(1L, "Prerna")
        );

        List<Transaction> transactions = List.of(
                new Transaction(
                        null,
                        customer,
                        BigDecimal.valueOf(120),
                        LocalDate.of(2024, 3, 15)
                ),
                new Transaction(
                        null,
                        customer,
                        BigDecimal.valueOf(75),
                        LocalDate.of(2024, 4, 10)
                )
        );

        mockMvc.perform(post("/api/rewards/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].customer.id").value(1))
                .andExpect(jsonPath("$[0].customer.name").value("Prerna"))
                .andExpect(jsonPath("$[1].amount").value(75));
    }

    @Test
    void testGetAllRewards() throws Exception {

        Customer customer = customerRepository.save(
                new Customer(1L, "Prerna")
        );

        transactionRepository.save(
                new Transaction(
                        null,
                        customer,
                        BigDecimal.valueOf(120),
                        LocalDate.of(2024, 3, 15)
                )
        );

        transactionRepository.save(
                new Transaction(
                        null,
                        customer,
                        BigDecimal.valueOf(75),
                        LocalDate.of(2024, 4, 10)
                )
        );

        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Prerna"));
    }

    @Test
    void testGetRewardsByCustomerId() throws Exception {

        Customer customer = customerRepository.save(
                new Customer(1L, "Rahul")
        );

        transactionRepository.save(
                createTransaction(
                        customer,
                        BigDecimal.valueOf(200),
                        LocalDate.of(2024, 5, 5)
                )
        );

        transactionRepository.save(
                createTransaction(
                        customer,
                        BigDecimal.valueOf(45),
                        LocalDate.of(2024, 6, 20)
                )
        );

        mockMvc.perform(
                        get("/api/rewards/{customerId}",
                                customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId")
                        .value(1))
                .andExpect(jsonPath("$.customerName")
                        .value("Rahul"));
    }

    @Test
    void testGetRewardsByDateRangeNoData() throws Exception {

        mockMvc.perform(get("/api/rewards/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isNotFound());
    }
}