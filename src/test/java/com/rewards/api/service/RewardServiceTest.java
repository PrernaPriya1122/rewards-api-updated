package com.rewards.api.service;

import com.rewards.api.dto.RewardResponse;
import com.rewards.api.model.Transaction;
import com.rewards.api.Repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@link RewardServiceImpl}
 * <p>
 * This class tests the functionality of saving transactions
 * and calculating reward points based on transaction amounts.
 * </p>
 */
class RewardServiceImplTest {

    /**
     * Mocked repository to simulate database operations.
     */
    @Mock
    private TransactionRepository transactionRepository;

    /**
     * Injects mocked dependencies into the service.
     */
    @InjectMocks
    private RewardServiceImpl rewardService;

    /**
     * Initializes mocks before each test case.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests saving a transaction using the repository.
     * Verifies that the saved transaction is returned correctly.
     */
    @Test
    void testSaveTransaction() {

        Transaction transaction = new Transaction();
        transaction.setCustomerId(1L);
        transaction.setCustomerName("Prerna");
        transaction.setAmount(new BigDecimal("120"));
        transaction.setTransactionDate(LocalDate.now());

        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction savedTransaction = rewardService.saveTransaction(transaction);

        assertNotNull(savedTransaction);
        assertEquals(1L, savedTransaction.getCustomerId());
        assertEquals("Prerna", savedTransaction.getCustomerName());
        assertEquals(new BigDecimal("120"), savedTransaction.getAmount());
    }

    /**
     * Tests reward calculation when transaction amount is less than 50.
     * Expected reward points = 0.
     */
    @Test
    void testCalculateRewards_ForAmountLessThan50() {

        Transaction transaction = new Transaction();
        transaction.setCustomerId(1L);
        transaction.setCustomerName("Prerna");
        transaction.setAmount(new BigDecimal("40"));
        transaction.setTransactionDate(LocalDate.of(2025, 1, 10));

        List<RewardResponse> responses =
                rewardService.calculateRewards(List.of(transaction));

        RewardResponse response = responses.get(0);

        assertEquals(0, response.getTotalPoints());
    }

    /**
     * Tests reward calculation when amount is between 50 and 100.
     * Expected reward points = (amount - 50).
     */
    @Test
    void testCalculateRewards_ForAmountBetween50And100() {

        Transaction transaction = new Transaction();
        transaction.setCustomerId(1L);
        transaction.setCustomerName("Prerna");
        transaction.setAmount(new BigDecimal("70"));
        transaction.setTransactionDate(LocalDate.of(2025, 1, 15));

        List<RewardResponse> responses =
                rewardService.calculateRewards(List.of(transaction));

        RewardResponse response = responses.get(0);

        assertEquals(20, response.getTotalPoints());
    }

    /**
     * Tests reward calculation when amount is greater than 100.
     * Expected reward points:
     * - 2 points for every dollar above 100
     * - 1 point for every dollar between 50 and 100
     */
    @Test
    void testCalculateRewards_ForAmountGreaterThan100() {

        Transaction transaction = new Transaction();
        transaction.setCustomerId(1L);
        transaction.setCustomerName("Prerna");
        transaction.setAmount(new BigDecimal("120"));
        transaction.setTransactionDate(LocalDate.of(2025, 2, 10));

        List<RewardResponse> responses =
                rewardService.calculateRewards(List.of(transaction));

        RewardResponse response = responses.get(0);

        assertEquals(90, response.getTotalPoints());
    }

    /**
     * Tests reward calculation for multiple transactions
     * across different months for the same customer.
     * Verifies total points and monthly breakdown.
     */
    @Test
    void testCalculateRewards_MultipleTransactions() {

        Transaction t1 = new Transaction();
        t1.setCustomerId(1L);
        t1.setCustomerName("Suman");
        t1.setAmount(new BigDecimal("120"));
        t1.setTransactionDate(LocalDate.of(2025, 1, 10));

        Transaction t2 = new Transaction();
        t2.setCustomerId(1L);
        t2.setCustomerName("Suman");
        t2.setAmount(new BigDecimal("80"));
        t2.setTransactionDate(LocalDate.of(2025, 2, 10));

        List<RewardResponse> responses =
                rewardService.calculateRewards(List.of(t1, t2));

        RewardResponse response = responses.get(0);

        Map<String, Integer> monthlyPoints =
                response.getMonthlyPoints();

        assertEquals(120, response.getTotalPoints());

        assertEquals(90, monthlyPoints.get("JANUARY"));
        assertEquals(30, monthlyPoints.get("FEBRUARY"));
    }

    /**
     * Tests reward calculation when the transaction amount is negative.
     * Verifies that an IllegalArgumentException is thrown.
     */
    @Test
    void testCalculateRewards_NegativeAmount() {

        Transaction transaction = new Transaction();
        transaction.setCustomerId(1L);
        transaction.setCustomerName("Suman");
        transaction.setAmount(new BigDecimal("-20"));
        transaction.setTransactionDate(LocalDate.now());

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> rewardService.calculateRewards(List.of(transaction))
        );

        assertEquals("Amount cannot be negative",
                exception.getMessage());
    }
}