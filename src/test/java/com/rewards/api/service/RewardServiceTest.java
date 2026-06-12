package com.rewards.api.service;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.model.Transaction;
import com.rewards.api.Repository.TransactionRepository;
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
 */
@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardServiceImpl rewardService;

    /**
     * Test saving a transaction successfully.
     * Ensures repository save is called and the persisted entity is returned.
     */
    @Test
    void testSaveTransaction() {
        Transaction transaction = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction saved = rewardService.saveTransaction(transaction);

        assertNotNull(saved);
        assertEquals("Prerna", saved.getCustomerName());
        assertEquals(BigDecimal.valueOf(120), saved.getAmount());
    }
    /**
     * Test reward calculation for amount <= 50.
     * Ensures no points are awarded.
     */
    @Test
    void testCalculatePointsAmountLessThanOrEqual50() {
        Transaction transaction = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(50), LocalDate.of(2024, 3, 15));

        List<RewardResponse> responses = rewardService.calculateRewards(List.of(transaction));

        assertEquals(BigDecimal.ZERO, responses.get(0).getTotalPoints());
    }

    /**
     * Test reward calculation for amount between 51 and 100.
     * Ensures points equal to (amount - 50).
     */
    @Test
    void testCalculatePointsAmountBetween50And100() {
        Transaction transaction = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(75), LocalDate.of(2024, 3, 15));

        List<RewardResponse> responses = rewardService.calculateRewards(List.of(transaction));

        assertEquals(BigDecimal.valueOf(25), responses.get(0).getTotalPoints());
    }

    /**
     * Test reward calculation for amount > 100.
     * Ensures points equal to (amount - 100) * 2 + 50.
     */
    @Test
    void testCalculatePointsAmountGreaterThan100() {
        Transaction transaction = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15));

        List<RewardResponse> responses = rewardService.calculateRewards(List.of(transaction));

        assertEquals(BigDecimal.valueOf(90), responses.get(0).getTotalPoints());
    }

    /**
     * Test reward calculation with multiple transactions across different months.
     * Ensures monthly breakdown and total aggregation are correct.
     */
    @Test
    void testCalculateRewardsMultipleTransactionsDifferentMonths() {
        Transaction t1 = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15));
        Transaction t2 = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(75), LocalDate.of(2025, 3, 15));

        List<RewardResponse> responses = rewardService.calculateRewards(List.of(t1, t2));

        Map<YearMonth, BigDecimal> monthlyPoints = responses.get(0).getMonthlyPoints();

        assertEquals(BigDecimal.valueOf(90), monthlyPoints.get(YearMonth.of(2024, 3)));
        assertEquals(BigDecimal.valueOf(25), monthlyPoints.get(YearMonth.of(2025, 3)));
        assertEquals(BigDecimal.valueOf(115), responses.get(0).getTotalPoints());
    }

    /**
     * Test negative amount throws IllegalArgumentException.
     */
    @Test
    void testNegativeAmountThrowsException() {
        Transaction transaction = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(-10), LocalDate.of(2024, 3, 15));

        assertThrows(IllegalArgumentException.class,
                () -> rewardService.calculateRewards(List.of(transaction)));
    }

    /**
     * Test calculateRewards with empty transaction list.
     * Ensures empty response is returned.
     */
    @Test
    void testCalculateRewardsEmptyList() {
        List<RewardResponse> responses = rewardService.calculateRewards(Collections.emptyList());
        assertTrue(responses.isEmpty());
    }

    /**
     * Test getAllRewards() fetches all transactions and calculates rewards.
     */
    @Test
    void testGetAllRewards() {
        Transaction t1 = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(120), LocalDate.of(2024, 3, 15));
        Transaction t2 = new Transaction(1L, "Prerna",
                BigDecimal.valueOf(75), LocalDate.of(2024, 4, 10));

        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2));

        List<RewardResponse> responses = rewardService.getAllRewards();

        assertEquals(1, responses.size());
        assertEquals(BigDecimal.valueOf(115), responses.get(0).getTotalPoints());
        assertEquals("Prerna", responses.get(0).getCustomerName());
        assertEquals(2, responses.get(0).getTransactions().size());
    }

    /**
     * Test getRewardsByCustomerId() when transactions exist.
     */
    @Test
    void testGetRewardsByCustomerIdFound() {
        Transaction t1 = new Transaction(2L, "Rahul",
                BigDecimal.valueOf(200), LocalDate.of(2024, 5, 5));
        Transaction t2 = new Transaction(2L, "Rahul",
                BigDecimal.valueOf(45), LocalDate.of(2024, 6, 20));

        when(transactionRepository.findByCustomerId(2L)).thenReturn(List.of(t1, t2));

        RewardResponse response = rewardService.getRewardsByCustomerId(2L);

        assertEquals(2L, response.getCustomerId());
        assertEquals("Rahul", response.getCustomerName());
        assertEquals(BigDecimal.valueOf(250), response.getTotalPoints());
        assertEquals(2, response.getTransactions().size());
    }

    /**
     * Test getRewardsByCustomerId() when no transactions exist.
     * Ensures RuntimeException is thrown.
     */
    @Test
    void testGetRewardsByCustomerIdNotFound() {
        when(transactionRepository.findByCustomerId(99L)).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> rewardService.getRewardsByCustomerId(99L));
    }
}
