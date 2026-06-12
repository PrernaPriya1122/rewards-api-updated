package com.rewards.api.Repository;
import com.rewards.api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * Finds all transactions for a given customer ID.
     * @param customerId the unique identifier of the customer
     * @return a list of {@link Transaction} objects associated with the given customer ID
     */
    List<Transaction> findByCustomerId(Long customerId);
}
