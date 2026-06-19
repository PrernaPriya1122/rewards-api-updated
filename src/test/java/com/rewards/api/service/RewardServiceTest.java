package com.rewards.api.service;

import com.rewards.api.Repository.CustomerRepository;
import com.rewards.api.Repository.TransactionRepository;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.exception.CustomerNotFoundException;
import com.rewards.api.exception.TransactionNotFoundException;
import com.rewards.api.model.Customer;
import com.rewards.api.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RewardServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private RewardServiceImpl rewardService;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                rewardService,
                "rewardMonths",
                3
        );
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
     * Verify rewards are calculated for multiple customers.
     */
    @Test
    void testGetAllRewards() {

        Customer customer1 = new Customer(1L, "John");
        Customer customer2 = new Customer(2L, "Jane");

        List<Transaction> transactions = List.of(
                createTransaction(
                        customer1,
                        BigDecimal.valueOf(120),
                        LocalDate.now().minusDays(10)
                ),
                createTransaction(
                        customer1,
                        BigDecimal.valueOf(75),
                        LocalDate.now().minusDays(5)
                ),
                createTransaction(
                        customer2,
                        BigDecimal.valueOf(150),
                        LocalDate.now().minusDays(15)
                )
        );

        when(transactionRepository.findAll())
                .thenReturn(transactions);

        List<RewardResponse> responses =
                rewardService.getAllRewards();

        assertEquals(2, responses.size());
    }

    /**
     * Verify reward calculation for a valid customer.
     */
    @Test
    void testGetRewardsByCustomerId() {

        Customer customer = new Customer(1L, "John");

        List<Transaction> transactions = List.of(
                createTransaction(
                        customer,
                        BigDecimal.valueOf(120),
                        LocalDate.now().minusDays(10)
                )
        );

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.findByCustomerId(1L))
                .thenReturn(transactions);

        RewardResponse response =
                rewardService.getRewardsByCustomerId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getCustomerId());
        assertEquals("John", response.getCustomerName());
    }

    /**
     * Verify CustomerNotFoundException is thrown
     * when customer does not exist.
     */
    @Test
    void testGetRewardsByCustomerIdCustomerNotFound() {

        when(customerRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> rewardService.getRewardsByCustomerId(100L)
        );
    }

    /**
     * Verify TransactionNotFoundException is thrown
     * when customer exists but has no transactions.
     */
    @Test
    void testGetRewardsByCustomerIdNoTransactions() {

        Customer customer = new Customer(1L, "John");

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(transactionRepository.findByCustomerId(1L))
                .thenReturn(List.of());

        assertThrows(
                TransactionNotFoundException.class,
                () -> rewardService.getRewardsByCustomerId(1L)
        );
    }

    /**
     * Verify rewards are fetched within a date range.
     */
    @Test
    void testGetRewardsByDateRange() {

        Customer customer = new Customer(1L, "John");

        List<Transaction> transactions = List.of(
                createTransaction(
                        customer,
                        BigDecimal.valueOf(120),
                        LocalDate.of(2026, 4, 10)
                )
        );

        when(transactionRepository.findByTransactionDateBetween(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).thenReturn(transactions);

        List<RewardResponse> responses =
                rewardService.getRewardsByDateRange(
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 4, 30)
                );

        assertFalse(responses.isEmpty());
    }

    /**
     * Verify exception when start date is null.
     */
    @Test
    void testGetRewardsByDateRangeWithNullDate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> rewardService.getRewardsByDateRange(
                        null,
                        LocalDate.now()
                )
        );
    }

    /**
     * Verify exception when start date is after end date.
     */
    @Test
    void testGetRewardsByDateRangeInvalidRange() {

        assertThrows(
                IllegalArgumentException.class,
                () -> rewardService.getRewardsByDateRange(
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 5, 1)
                )
        );
    }

    /**
     * Verify exception when no transactions exist
     * in the specified date range.
     */
    @Test
    void testGetRewardsByDateRangeNoTransactions() {

        when(transactionRepository.findByTransactionDateBetween(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).thenReturn(List.of());

        assertThrows(
                TransactionNotFoundException.class,
                () -> rewardService.getRewardsByDateRange(
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 4, 30)
                )
        );
    }

    /**
     * Verify negative amount transaction throws exception.
     */
    @Test
    void testCalculateRewardsWithNegativeAmount() {

        Customer customer = new Customer(1L, "John");

        List<Transaction> transactions = List.of(
                createTransaction(
                        customer,
                        BigDecimal.valueOf(-100),
                        LocalDate.now()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rewardService.calculateRewards(transactions)
        );
    }

    /**
     * Verify reward calculation for multiple months.
     */
    @Test
    void testCalculateRewardsMultipleMonths() {

        Customer customer = new Customer(1L, "John");

        List<Transaction> transactions = List.of(
                createTransaction(
                        customer,
                        BigDecimal.valueOf(120),
                        LocalDate.of(2026, 4, 10)
                ),
                createTransaction(
                        customer,
                        BigDecimal.valueOf(200),
                        LocalDate.of(2026, 5, 15)
                ),
                createTransaction(
                        customer,
                        BigDecimal.valueOf(75),
                        LocalDate.of(2026, 6, 5)
                )
        );

        List<RewardResponse> responses =
                rewardService.calculateRewards(transactions);

        assertEquals(1, responses.size());
        assertEquals(
                3,
                responses.get(0).getMonthlyRewards().size()
        );
    }
}