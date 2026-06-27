package com.treasuryflow.repository;

import com.treasuryflow.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * TransactionRepository - Spring Data JPA Repository for Transaction entity
 *
 * @Repository - Registers as Spring bean.
 * JpaRepository<Transaction, Long> - Standard CRUD for Transaction with Long ID.
 *
 * CUSTOM QUERY METHODS (using @Query annotation for complex queries):
 *
 * Spring Data JPA supports two ways to define queries:
 * 1. QUERY DERIVATION - Method name follows naming convention (auto-generated SQL)
 * 2. @Query ANNOTATION - Explicit JPQL or native SQL for complex queries
 *
 * @Query(value = "...", nativeQuery = true) - Uses native SQL (MySQL dialect)
 * @Query("SELECT t FROM Transaction t WHERE ...") - Uses JPQL (entity-based)
 *
 * JPQL (Java Persistence Query Language):
 * - Operates on ENTITY OBJECTS, not database tables
 * - Uses entity field names (e.g., t.amount, t.account.id)
 * - Database-agnostic (works with any JPA provider)
 * - Example: "SELECT t FROM Transaction t WHERE t.account.id = :accountId"
 *
 * Native SQL:
 * - Direct database SQL
 * - Uses actual table/column names
 * - Database-specific features available
 * - Example: "SELECT * FROM transactions WHERE account_id = ?1"
 *
 * PARAMETER BINDING:
 * - ?1, ?2, ... - Positional parameters (1-based index)
 * - :paramName - Named parameters (use with @Param("paramName"))
 * - Named parameters are preferred for readability
 *
 * RETURN TYPES:
 * - Entity (Transaction) - Full entity objects
 * - Projection (interface/class) - Partial data
 * - Scalar (BigDecimal, Long, etc.) - Single values
 * - List<T> - Multiple results
 * - Optional<T> - Single result or empty
 * - Page<T> / Slice<T> - Paginated results
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ============================================================
    // QUERY DERIVATION METHODS (Auto-generated from method names)
    // ============================================================

    // findByAccountIdOrderByTransactionDateDesc(Long accountId) →
    // SELECT * FROM transactions WHERE account_id = ?1 ORDER BY transaction_date DESC
    // Spring Data JPA parses:
    //   "findBy" → SELECT *
    //   "AccountId" → WHERE account_id = ? (account is @ManyToOne field, uses its id)
    //   "OrderBy" → ORDER BY
    //   "TransactionDateDesc" → transaction_date DESC
    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);

    // findByAccountIdAndTransactionDateBetween(Long accountId, LocalDate start, LocalDate end) →
    // SELECT * FROM transactions
    // WHERE account_id = ?1 AND transaction_date BETWEEN ?2 AND ?3
    // "Between" keyword generates BETWEEN clause with two parameters
    List<Transaction> findByAccountIdAndTransactionDateBetween(
            Long accountId, LocalDate start, LocalDate end);

    // findByAccountIdAndType(Long accountId, Transaction.TransactionType type) →
    // SELECT * FROM transactions WHERE account_id = ?1 AND type = ?2
    // Enum parameter automatically converted to database ENUM value
    List<Transaction> findByAccountIdAndType(Long accountId, Transaction.TransactionType type);

    // findByAccountIdAndCategory(Long accountId, String category) →
    // SELECT * FROM transactions WHERE account_id = ?1 AND category = ?2
    List<Transaction> findByAccountIdAndCategory(Long accountId, String category);

    // findByReferenceNo(String referenceNo) →
    // SELECT * FROM transactions WHERE reference_no = ?1
    Transaction findByReferenceNo(String referenceNo);

    // ============================================================
    // CUSTOM @QUERY METHODS (Explicit JPQL for complex aggregations)
    // ============================================================

    /**
     * sumAmountByAccountIdAndType - Calculate sum of amounts by account and transaction type
     *
     * JPQL EXPLANATION:
     * "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
     *  WHERE t.account.id = :accountId AND t.type = :type"
     *
     * - COALESCE(SUM(t.amount), 0) → Returns 0 instead of NULL if no matching rows
     * - t.account.id → Navigates @ManyToOne relationship to BankAccount, then gets its id
     * - :accountId, :type → Named parameters bound via @Param annotation
     * - Returns BigDecimal (matches Transaction.amount field type)
     *
     * GENERATED SQL (approximate):
     * SELECT COALESCE(SUM(amount), 0) FROM transactions
     * WHERE account_id = ? AND type = ?
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account.id = :accountId AND t.type = :type")
    BigDecimal sumAmountByAccountIdAndType(
            @Param("accountId") Long accountId,
            @Param("type") Transaction.TransactionType type);

    /**
     * Alternative: Using native SQL for the same aggregation
     * Native query uses actual table/column names
     */
    // @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
    //        "WHERE account_id = :accountId AND type = :type", nativeQuery = true)
    // BigDecimal sumAmountByAccountIdAndTypeNative(
    //         @Param("accountId") Long accountId,
    //         @Param("type") String type);

    /**
     * sumAmountByAccountIdAndDateRange - Sum amounts within date range
     * JPQL: COALESCE returns 0 if no transactions match
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account.id = :accountId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * sumCreditByAccountId - Total credits (inflow) for an account
     * Uses TransactionType.CREDIT enum value
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account.id = :accountId AND t.type = 'CREDIT'")
    BigDecimal sumCreditByAccountId(@Param("accountId") Long accountId);

    /**
     * sumDebitByAccountId - Total debits (outflow) for an account
     * Uses TransactionType.DEBIT enum value
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.account.id = :accountId AND t.type = 'DEBIT'")
    BigDecimal sumDebitByAccountId(@Param("accountId") Long accountId);

    /**
     * countByAccountId - Count transactions for an account
     * Returns Long (count)
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId")
    Long countByAccountId(@Param("accountId") Long accountId);
}