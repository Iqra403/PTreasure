package com.treasuryflow.repository;

import com.treasuryflow.model.CashFlowSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CashFlowSummaryRepository - Spring Data JPA Repository for CashFlowSummary entity
 *
 * @Repository - Spring bean registration.
 * JpaRepository<CashFlowSummary, Long> - Standard CRUD with Long primary key.
 *
 * UNIQUE CONSTRAINT HANDLING:
 * The CashFlowSummary entity has @UniqueConstraint on (account_id, summary_month).
 * This means: one summary per account per month.
 *
 * When saving, if a record with same account_id + summary_month exists:
 * - save() will UPDATE the existing record (because @Id is auto-generated, but unique constraint triggers conflict)
 * - For true upsert behavior, consider @Modifying @Query with ON DUPLICATE KEY UPDATE (MySQL)
 *   or use Spring Data's save() which handles this if ID is provided
 *
 * QUERY DERIVATION EXAMPLES:
 * findByAccountId(Long accountId)              → WHERE account_id = ?
 * findByAccountIdAndSummaryMonth(Long, LocalDate) → WHERE account_id = ? AND summary_month = ?
 * findBySummaryMonthBetween(LocalDate, LocalDate)  → WHERE summary_month BETWEEN ? AND ?
 * findByAccountIdOrderBySummaryMonthAsc(Long)      → WHERE account_id = ? ORDER BY summary_month ASC
 * findTop12ByAccountIdOrderBySummaryMonthDesc(Long) → Last 12 months summary
 */
@Repository
public interface CashFlowSummaryRepository extends JpaRepository<CashFlowSummary, Long> {

    // ============================================================
    // QUERY DERIVATION METHODS
    // ============================================================

    // findByAccountIdOrderBySummaryMonthAsc(Long accountId) →
    // SELECT * FROM cash_flow_summary WHERE account_id = ?1 ORDER BY summary_month ASC
    // Spring Data JPA parses:
    //   "findBy" → SELECT *
    //   "AccountId" → WHERE account_id = ? (account is @ManyToOne field)
    //   "OrderBy" → ORDER BY
    //   "SummaryMonthAsc" → summary_month ASC
    // Returns List ordered chronologically (oldest first)
    List<CashFlowSummary> findByAccountIdOrderBySummaryMonthAsc(Long accountId);

    // findByAccountIdAndSummaryMonth(Long accountId, LocalDate summaryMonth) →
    // SELECT * FROM cash_flow_summary WHERE account_id = ?1 AND summary_month = ?2
    // Uses the unique constraint columns for exact match
    // Returns Optional (0 or 1 result due to unique constraint)
    Optional<CashFlowSummary> findByAccountIdAndSummaryMonth(Long accountId, LocalDate summaryMonth);

    // findByAccountIdAndSummaryMonthBetween(Long accountId, LocalDate start, LocalDate end) →
    // SELECT * FROM cash_flow_summary
    // WHERE account_id = ?1 AND summary_month BETWEEN ?2 AND ?3
    // Ordered by summary_month ascending by default (or add OrderBySummaryMonthAsc)
    List<CashFlowSummary> findByAccountIdAndSummaryMonthBetween(
            Long accountId, LocalDate start, LocalDate end);

    // findTop12ByAccountIdOrderBySummaryMonthDesc(Long accountId) →
    // SELECT * FROM cash_flow_summary
    // WHERE account_id = ?1 ORDER BY summary_month DESC LIMIT 12
    // "Top12" / "First12" → LIMIT 12
    // "Desc" → Descending order (newest first)
    // Useful for "last 12 months" dashboard view
    List<CashFlowSummary> findTop12ByAccountIdOrderBySummaryMonthDesc(Long accountId);

    // existsByAccountIdAndSummaryMonth(Long accountId, LocalDate summaryMonth) →
    // SELECT COUNT(*) > 0 FROM cash_flow_summary WHERE account_id = ?1 AND summary_month = ?2
    // Boolean check before insert (alternative to Optional find)
    boolean existsByAccountIdAndSummaryMonth(Long accountId, LocalDate summaryMonth);

    // ============================================================
    // CUSTOM @QUERY METHODS FOR AGGREGATIONS
    // ============================================================

    /**
     * getLatestSummary - Get the most recent cash flow summary for an account
     * JPQL: ORDER BY summary_month DESC with LIMIT 1 (via findFirst)
     * Alternative to findTop1ByAccountIdOrderBySummaryMonthDesc
     */
    @Query("SELECT c FROM CashFlowSummary c " +
           "WHERE c.account.id = :accountId " +
           "ORDER BY c.summaryMonth DESC")
    Optional<CashFlowSummary> getLatestSummary(@Param("accountId") Long accountId);

    /**
     * getTotalNetFlowByAccountId - Sum of net_flow for all months of an account
     * Useful for lifetime cash flow overview
     */
    @Query("SELECT COALESCE(SUM(c.netFlow), 0) FROM CashFlowSummary c " +
           "WHERE c.account.id = :accountId")
    java.math.BigDecimal getTotalNetFlowByAccountId(@Param("accountId") Long accountId);

    /**
     * getTotalInflowByAccountId - Sum of total_inflow for an account
     */
    @Query("SELECT COALESCE(SUM(c.totalInflow), 0) FROM CashFlowSummary c " +
           "WHERE c.account.id = :accountId")
    java.math.BigDecimal getTotalInflowByAccountId(@Param("accountId") Long accountId);

    /**
     * getTotalOutflowByAccountId - Sum of total_outflow for an account
     */
    @Query("SELECT COALESCE(SUM(c.totalOutflow), 0) FROM CashFlowSummary c " +
           "WHERE c.account.id = :accountId")
    java.math.BigDecimal getTotalOutflowByAccountId(@Param("accountId") Long accountId);

    /**
     * findByAccountIdAndSummaryMonthAfter - Summaries after a specific month
     * JPQL with named parameter for clarity
     */
    @Query("SELECT c FROM CashFlowSummary c " +
           "WHERE c.account.id = :accountId AND c.summaryMonth > :afterMonth " +
           "ORDER BY c.summaryMonth ASC")
    List<CashFlowSummary> findByAccountIdAndSummaryMonthAfter(
            @Param("accountId") Long accountId,
            @Param("afterMonth") LocalDate afterMonth);

    /**
     * countSummariesByAccountId - Number of monthly summaries for an account
     */
    @Query("SELECT COUNT(c) FROM CashFlowSummary c WHERE c.account.id = :accountId")
    Long countSummariesByAccountId(@Param("accountId") Long accountId);
}