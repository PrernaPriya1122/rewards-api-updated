package com.rewards.api.service;
import com.rewards.api.Repository.TransactionRepository;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.model.Customer;
import com.rewards.api.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RewardServiceImpl}.
 * Covers all reward calculation logic, repository interactions,
 * and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardServiceImpl rewardService;

    /**
     * Utility method to create a transaction with Customer.
     */
    private Transaction createTransaction(Long customerId, String name,
                                          BigDecimal amount, LocalDate date) {

        Customer customer = new Customer(customerId, name);
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setAmount(amount);
        transaction.setTransactionDate(date);

        return transaction;
    }

    /**
     * Test saving a transaction successfully.
     */
    @Test
    void testSaveTransaction() {

        Transaction input = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(input);

        Transaction saved = rewardService.saveTransaction(input);

        assertNotNull(saved);
        assertEquals("Prerna", saved.getCustomer().getName());
        assertEquals(BigDecimal.valueOf(120), saved.getAmount());
    }

    /**
     * Test reward calculation for amount <= 50.
     */
    @Test
    void testCalculatePointsAmountLessThanOrEqual50() {

        Transaction transaction = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(50), LocalDate.now());

        List<RewardResponse> responses = rewardService.calculateRewards(List.of(transaction));

        assertEquals(BigDecimal.ZERO, responses.get(0).getTotalPoints());
    }

    /**
     * Test reward calculation for amount between 50 and 100.
     */
    @Test
    void testCalculatePointsAmountBetween50And100() {

        Transaction transaction = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(75), LocalDate.now());

        List<RewardResponse> responses = rewardService.calculateRewards(List.of(transaction));

        assertEquals(BigDecimal.valueOf(25), responses.get(0).getTotalPoints());
    }

    /**
     * Test reward calculation for amount > 100.
     */
    @Test
    void testCalculatePointsAmountGreaterThan100() {

        Transaction transaction = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.now());

        List<RewardResponse> responses = rewardService.calculateRewards(List.of(transaction));

        assertEquals(BigDecimal.valueOf(90), responses.get(0).getTotalPoints());
    }

    /**
     * Test multiple transactions across different months.
     */
    @Test
    void testCalculateRewardsMultipleMonths() {

        Transaction t1 = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15));

        Transaction t2 = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(75), LocalDate.of(2025, 3, 15));

        List<RewardResponse> responses = rewardService.calculateRewards(List.of(t1, t2));

        Map<YearMonth, BigDecimal> monthly = responses.get(0).getMonthlyPoints();

        assertEquals(BigDecimal.valueOf(90), monthly.get(YearMonth.of(2024, 3)));
        assertEquals(BigDecimal.valueOf(25), monthly.get(YearMonth.of(2025, 3)));
        assertEquals(BigDecimal.valueOf(115), responses.get(0).getTotalPoints());
    }

    /**
     * Test negative amount throws exception.
     */
    @Test
    void testNegativeAmountThrowsException() {

        Transaction transaction = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(-10), LocalDate.now());

        assertThrows(IllegalArgumentException.class,
                () -> rewardService.calculateRewards(List.of(transaction)));
    }

    /**
     * Test empty transaction list returns empty result.
     */
    @Test
    void testEmptyTransactionList() {

        List<RewardResponse> responses = rewardService.calculateRewards(Collections.emptyList());

        assertTrue(responses.isEmpty());
    }

    /**
     * Test getAllRewards with repository data.
     */
    @Test
    void testGetAllRewards() {

        Transaction t1 = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15));

        Transaction t2 = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(75), LocalDate.of(2024, 4, 15));

        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2));

        List<RewardResponse> responses = rewardService.getAllRewards();

        assertEquals(1, responses.size());
        assertEquals(BigDecimal.valueOf(115), responses.get(0).getTotalPoints());
        assertEquals("Prerna", responses.get(0).getCustomerName());
    }

    /**
     * Test getRewardsByCustomerId when data exists.
     */
    @Test
    void testGetRewardsByCustomerIdFound() {

        Transaction t1 = createTransaction(2L, "Rahul",
                BigDecimal.valueOf(200), LocalDate.now());

        Transaction t2 = createTransaction(2L, "Rahul",
                BigDecimal.valueOf(40), LocalDate.now());

        when(transactionRepository.findByCustomerId(2L)).thenReturn(List.of(t1, t2));

        RewardResponse response = rewardService.getRewardsByCustomerId(2L);

        assertEquals(2L, response.getCustomerId());
        assertEquals("Rahul", response.getCustomerName());
        assertEquals(BigDecimal.valueOf(250), response.getTotalPoints());
    }

    /**
     * Test getRewardsByCustomerId when no data exists.
     */
    @Test
    void testGetRewardsByCustomerIdNotFound() {

        when(transactionRepository.findByCustomerId(99L)).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class,
                () -> rewardService.getRewardsByCustomerId(99L));
    }

    /**
     * Test getRewardsByDateRange success scenario.
     */
    @Test
    void testGetRewardsByDateRangeSuccess() {

        Transaction t1 = createTransaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.of(2024, 1, 10));

        when(transactionRepository.findByTransactionDateBetween(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31)))
                .thenReturn(List.of(t1));

        List<RewardResponse> responses =
                rewardService.getRewardsByDateRange(
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 1, 31));

        assertEquals(1, responses.size());
        assertEquals(BigDecimal.valueOf(90), responses.get(0).getTotalPoints());
    }

    /**
     * Test getRewardsByDateRange with null dates.
     */
    @Test
    void testGetRewardsByDateRangeNullDates() {

        assertThrows(IllegalArgumentException.class,
                () -> rewardService.getRewardsByDateRange(null, LocalDate.now()));
    }

    /**
     * Test getRewardsByDateRange when start date is after end date.
     */
    @Test
    void testGetRewardsByDateRangeInvalidRange() {

        assertThrows(IllegalArgumentException.class,
                () -> rewardService.getRewardsByDateRange(
                        LocalDate.of(2024, 5, 1),
                        LocalDate.of(2024, 1, 1)));
    }

    /**
     * Test getRewardsByDateRange when no transactions found.
     */
    @Test
    void testGetRewardsByDateRangeNoData() {

        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class,
                () -> rewardService.getRewardsByDateRange(
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 1, 31)));
    }
}