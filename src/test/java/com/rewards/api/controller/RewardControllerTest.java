package com.rewards.api.controller;
import com.rewards.api.dto.MonthlyReward;
import com.rewards.api.dto.RewardResponse;
import com.rewards.api.exception.CustomerNotFoundException;
import com.rewards.api.exception.TransactionNotFoundException;
import com.rewards.api.service.RewardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link RewardController}.
 *
 * <p>
 * Tests controller endpoints using MockMvc while
 * mocking the service layer.
 * </p>
 */
@WebMvcTest(RewardController.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardService rewardService;

    /**
     * Verify all rewards are returned successfully
     * for multiple customers.
     */
    @Test
    void testGetAllRewards() throws Exception {

        RewardResponse customerOne =
                new RewardResponse(
                        1L,
                        "John",
                        List.of(
                                new MonthlyReward(
                                        "2026-04",
                                        BigDecimal.valueOf(90))
                        ),
                        BigDecimal.valueOf(90)
                );

        RewardResponse customerTwo =
                new RewardResponse(
                        2L,
                        "Jane",
                        List.of(
                                new MonthlyReward(
                                        "2026-05",
                                        BigDecimal.valueOf(150))
                        ),
                        BigDecimal.valueOf(150)
                );

        when(rewardService.getAllRewards())
                .thenReturn(List.of(customerOne, customerTwo));

        mockMvc.perform(get("/api/rewards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerName").value("John"))
                .andExpect(jsonPath("$[1].customerId").value(2))
                .andExpect(jsonPath("$[1].customerName").value("Jane"));
    }

    /**
     * Verify rewards are returned successfully
     * for a valid customer.
     */
    @Test
    void testGetRewardsByCustomerId() throws Exception {

        RewardResponse response =
                new RewardResponse(
                        1L,
                        "John",
                        List.of(
                                new MonthlyReward(
                                        "2026-04",
                                        BigDecimal.valueOf(90))
                        ),
                        BigDecimal.valueOf(90)
                );

        when(rewardService.getRewardsByCustomerId(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerName").value("John"))
                .andExpect(jsonPath("$.totalRewards").value(90));
    }

    /**
     * Verify bad request is returned when
     * customer id is zero.
     */
    @Test
    void testGetRewardsByCustomerIdWithZeroId() throws Exception {

        mockMvc.perform(get("/api/rewards/0"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Customer ID must be greater than zero"));
    }

    /**
     * Verify bad request is returned when
     * customer id is negative.
     */
    @Test
    void testGetRewardsByCustomerIdWithNegativeId() throws Exception {

        mockMvc.perform(get("/api/rewards/-1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verify bad request is returned when
     * customer id is not numeric.
     */
    @Test
    void testGetRewardsByCustomerIdWithInvalidId() throws Exception {

        mockMvc.perform(get("/api/rewards/test"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verify customer not found scenario.
     */
    @Test
    void testGetRewardsByCustomerIdCustomerNotFound()
            throws Exception {

        when(rewardService.getRewardsByCustomerId(100L))
                .thenThrow(
                        new CustomerNotFoundException(
                                "Customer not found with ID"));

        mockMvc.perform(get("/api/rewards/100"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verify transaction not found scenario.
     */
    @Test
    void testGetRewardsByCustomerIdTransactionNotFound()
            throws Exception {

        when(rewardService.getRewardsByCustomerId(1L))
                .thenThrow(
                        new TransactionNotFoundException(
                                "No transactions found for customer ID"));

        mockMvc.perform(get("/api/rewards/1"))
                .andExpect(status().isNotFound());
    }
    /**
     * Verify rewards are returned for
     * a valid date range.
     */
    @Test
    void testGetRewardsByDateRange() throws Exception {

        RewardResponse response =
                new RewardResponse(
                        1L,
                        "John",
                        List.of(
                                new MonthlyReward(
                                        "2026-04",
                                        BigDecimal.valueOf(90))
                        ),
                        BigDecimal.valueOf(90)
                );

        when(rewardService.getRewardsByDateRange(
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/rewards/date-range")
                                .param(
                                        "startDate",
                                        "2026-04-01")
                                .param(
                                        "endDate",
                                        "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId")
                        .value(1));
    }

    /**
     * Verify invalid date range scenario.
     */
    @Test
    void testGetRewardsByDateRangeInvalidRange()
            throws Exception {

        when(rewardService.getRewardsByDateRange(
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenThrow(
                        new IllegalArgumentException(
                                "Start date must be before end date"));

        mockMvc.perform(
                        get("/api/rewards/date-range")
                                .param(
                                        "startDate",
                                        "2026-06-30")
                                .param(
                                        "endDate",
                                        "2026-04-01"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verify no transactions found
     * for date range scenario.
     */
    @Test
    void testGetRewardsByDateRangeNoTransactions()
            throws Exception {

        when(rewardService.getRewardsByDateRange(
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenThrow(
                        new TransactionNotFoundException(
                                "No transactions found between given dates"));

        mockMvc.perform(
                        get("/api/rewards/date-range")
                                .param(
                                        "startDate",
                                        "2026-04-01")
                                .param(
                                        "endDate",
                                        "2026-04-30"))
                .andExpect(status().isNotFound());
    }
}