package com.rewards.api.controller;
import com.rewards.api.Repository.CustomerRepository;
import com.rewards.api.model.Customer;
import com.rewards.api.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.rewards.api.Repository.TransactionRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for RewardController.
 *
 * <p>Tests complete flow:
 * Controller -> Service -> Repository -> Database.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class RewardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Clean database before each test.
     */
    @BeforeEach
    void setUp() {

        transactionRepository.deleteAll();
        customerRepository.deleteAll();
    }

    /**
     * Creates a transaction for testing.
     */
    private Transaction createTransaction(
            Customer customer,
            BigDecimal amount,
            LocalDate date) {

        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(amount);
        transaction.setTransactionDate(date);

        return transaction;
    }

    /**
     * Verify rewards are calculated for multiple customers
     * having multiple transactions.
     */
    @Test
    void testGetAllRewardsForMultipleCustomers() throws Exception {

        Customer customerOne =
                customerRepository.save(
                        new Customer(201L, "John"));

        Customer customerTwo =
                customerRepository.save(
                        new Customer(202L, "Jane"));

        transactionRepository.save(
                createTransaction(
                        customerOne,
                        BigDecimal.valueOf(120),
                        LocalDate.now().minusDays(10)));

        transactionRepository.save(
                createTransaction(
                        customerOne,
                        BigDecimal.valueOf(75),
                        LocalDate.now().minusDays(8)));

        transactionRepository.save(
                createTransaction(
                        customerTwo,
                        BigDecimal.valueOf(150),
                        LocalDate.now().minusDays(5)));

        transactionRepository.save(
                createTransaction(
                        customerTwo,
                        BigDecimal.valueOf(200),
                        LocalDate.now().minusDays(2)));

        mockMvc.perform(get("/api/rewards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").exists())
                .andExpect(jsonPath("$[0].monthlyRewards").exists())
                .andExpect(jsonPath("$[1].customerId").exists())
                .andExpect(jsonPath("$[1].monthlyRewards").exists());
    }

    /**
     * Verify rewards are returned for a valid customer.
     */
    @Test
    void testGetRewardsByCustomerId() throws Exception {

        Customer customer =
                customerRepository.save(
                        new Customer(100L, "Prerna"));

        transactionRepository.save(
                createTransaction(
                        customer,
                        BigDecimal.valueOf(120),
                        LocalDate.now().minusDays(5)));

        mockMvc.perform(
                        get("/api/rewards/{customerId}",
                                customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId")
                        .value(customer.getId()))
                .andExpect(jsonPath("$.customerName")
                        .value("Prerna"));
    }

    /**
     * Verify customer not found scenario.
     */
    @Test
    void testCustomerNotFound() throws Exception {

        mockMvc.perform(get("/api/rewards/999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verify validation failure when customer id is zero.
     */
    @Test
    void testCustomerIdZero() throws Exception {

        mockMvc.perform(get("/api/rewards/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Customer ID must be greater than zero"));
    }

    /**
     * Verify validation failure when customer id is negative.
     */
    @Test
    void testNegativeCustomerId() throws Exception {

        mockMvc.perform(get("/api/rewards/-1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verify invalid customer id datatype.
     */
    @Test
    void testCustomerIdNonNumeric() throws Exception {

        mockMvc.perform(get("/api/rewards/test"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verify rewards are returned for valid date range.
     */
    @Test
    void testGetRewardsByDateRange() throws Exception {

        Customer customer =
                customerRepository.save(
                        new Customer(101L, "John"));

        transactionRepository.save(
                createTransaction(
                        customer,
                        BigDecimal.valueOf(150),
                        LocalDate.now().minusDays(10)));

        mockMvc.perform(
                        get("/api/rewards/date-range")
                                .param("startDate",
                                        LocalDate.now().minusDays(30).toString())
                                .param("endDate",
                                        LocalDate.now().toString()))
                .andExpect(status().isOk());
    }

    /**
     * Verify exception when start date is after end date.
     */
    @Test
    void testInvalidDateRange() throws Exception {

        mockMvc.perform(
                        get("/api/rewards/date-range")
                                .param("startDate",
                                        "2026-06-30")
                                .param("endDate",
                                        "2026-04-01"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verify no transactions found between dates.
     */
    @Test
    void testNoTransactionsFoundForDateRange()
            throws Exception {

        mockMvc.perform(
                        get("/api/rewards/date-range")
                                .param("startDate",
                                        "2026-01-01")
                                .param("endDate",
                                        "2026-01-31"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verify customer exists but has no transactions.
     */
    @Test
    void testCustomerWithNoTransactions()
            throws Exception {

        Customer customer =
                customerRepository.save(
                        new Customer(102L, "Empty Customer"));

        mockMvc.perform(
                        get("/api/rewards/{customerId}",
                                customer.getId()))
                .andExpect(status().isNotFound());
    }

}



