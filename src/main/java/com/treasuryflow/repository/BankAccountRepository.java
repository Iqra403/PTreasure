package com.treasuryflow.repository;

import com.treasuryflow.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * BankAccountRepository - Spring Data JPA Repository for BankAccount entity
 *
 * @Repository - Registers this as a Spring bean (repository component).
 *               Spring auto-detects and proxies this interface.
 *
 * JpaRepository<BankAccount, Long> - Provides all standard CRUD operations
 *   for BankAccount entity with Long primary key.
 *
 * QUERY DERIVATION EXAMPLES (auto-generated from method names):
 *
 * findByUserId(Long userId)         → SELECT * FROM bank_accounts WHERE user_id = ?
 * findByUserAndIsActiveTrue()       → SELECT * FROM bank_accounts WHERE user_id = ? AND is_active = true
 * findByBankNameContaining(String)  → SELECT * FROM bank_accounts WHERE bank_name LIKE %?%
 * findByAccountNumber(String)       → SELECT * FROM bank_accounts WHERE account_number = ?
 * findByCurrency(String currency)   → SELECT * FROM bank_accounts WHERE currency = ?
 *
 * The "User" in findByUser refers to the @ManyToOne field in BankAccount entity.
 * Spring Data JPA automatically joins the users table via the foreign key.
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    // findByUserId(Long userId) → SELECT * FROM bank_accounts WHERE user_id = ?1
    // Spring Data JPA recognizes "User" as the field name in BankAccount entity
    // and automatically maps it to the user_id foreign key column
    List<BankAccount> findByUserId(Long userId);

    // findByUserIdAndIsActiveTrue(Long userId) →
    // SELECT * FROM bank_accounts WHERE user_id = ?1 AND is_active = true
    // Boolean property "isActive" becomes "is_active" column with = true condition
    List<BankAccount> findByUserIdAndIsActiveTrue(Long userId);

    // findByAccountNumber(String accountNumber) →
    // SELECT * FROM bank_accounts WHERE account_number = ?1
    BankAccount findByAccountNumber(String accountNumber);

    // findByBankNameContainingIgnoreCase(String bankName) →
    // SELECT * FROM bank_accounts WHERE LOWER(bank_name) LIKE LOWER(%?1%)
    // Case-insensitive partial match on bank name
    List<BankAccount> findByBankNameContainingIgnoreCase(String bankName);

    // existsByUserIdAndAccountNumber(Long userId, String accountNumber) →
    // SELECT COUNT(*) > 0 FROM bank_accounts WHERE user_id = ?1 AND account_number = ?2
    // Useful for checking duplicate account numbers per user
    boolean existsByUserIdAndAccountNumber(Long userId, String accountNumber);
}