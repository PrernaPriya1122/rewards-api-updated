package com.rewards.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Entity class representing a transaction in the rewards system.
 * Each transaction is linked to a customer and contains details such as
 * the transaction amount and date. This entity is persisted in the
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    /** Auto-generated primary key for the transaction. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique identifier of the customer associated with the transaction. */
    @NotNull(message = "Customer id is required")
    private Long customerId;

    /** Name of the customer. */
    @NotBlank(message = "Customer name is required")
    private String customerName;

    /** Monetary value of the transaction (must be greater than 0). */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    /** Date when the transaction occurred. */
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    /**
     * Default constructor required by JPA.
     */
    public Transaction() {
    }

    /**
     * Constructs a new {@code Transaction}.
     *
     * @param customerId      the unique identifier of the customer
     * @param customerName    the name of the customer
     * @param amount          the monetary value of the transaction
     * @param transactionDate the date when the transaction occurred
     */
    public Transaction(Long customerId,
                       String customerName,
                       BigDecimal amount,
                       LocalDate transactionDate) {

        this.customerId = customerId;
        this.customerName = customerName;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }

    /** @return the customer ID */
    public Long getCustomerId() {
        return customerId;
    }

    /** @return the customer name */
    public String getCustomerName() {
        return customerName;
    }

    /** @return the transaction amount */
    public BigDecimal getAmount() {
        return amount;
    }

    /** @return the transaction date */
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    /** Sets the customer ID. */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /** Sets the customer name. */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /** Sets the transaction amount. */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /** Sets the transaction date. */
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
}
