package com.treasuryflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CashFlowSummary Entity - Maps to the 'cash_flow_summary' table in MySQL
 *
 * ANNOTATION EXPLANATIONS:
 *
 * @Entity - JPA entity managed by persistence context.
 *
 * @Table(name = "cash_flow_summary", uniqueConstraints = @UniqueConstraint(...))
 *        - Maps to 'cash_flow_summary' table.
 *        - uniqueConstraints defines database-level unique constraints.
 *          Here: UNIQUE KEY unique_account_month (account_id, summary_month)
 *          Prevents duplicate summaries for the same account in the same month.
 *
 * @Data - Lombok boilerplate reduction.
 *
 * @Id + @GeneratedValue(IDENTITY) - Auto-increment primary key.
 *
 * @ManyToOne - Many CashFlowSummaries belong to ONE BankAccount.
 *              Owning side with foreign key 'account_id'.
 *
 * @JoinColumn - Foreign key column in THIS table (cash_flow_summary).
 *               name = "account_id" - column in cash_flow_summary.
 *               referencedColumnName = "id" - column in bank_accounts.
 *               Creates: FOREIGN KEY (account_id) REFERENCES bank_accounts(id)
 *
 * @Column(precision = 15, scale = 2) - Maps to DECIMAL(15,2) for monetary values.
 *
 * @Temporal(TemporalType.DATE) - Maps LocalDate to DATE column (summary_month).
 *              Note: summary_month stores the first day of the month (e.g., 2024-01-01)
 *              to represent a specific month.
 *
 * @Column(insertable = false, updatable = false) - For columns with DEFAULT values
 *              managed by database (like updated_at with ON UPDATE CURRENT_TIMESTAMP).
 *              JPA won't try to insert/update these; database handles them.
 *
 * UniqueConstraint - Defines a unique constraint at the database level.
 *              columnNames = {"account_id", "summary_month"} creates:
 *              UNIQUE KEY unique_account_month (account_id, summary_month)
 *              This ensures one summary per account per month.
 */
@Entity
@Table(
    name = "cash_flow_summary",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_account_month",
        columnNames = {"account_id", "summary_month"}
    )
)
@Data
public class CashFlowSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Many CashFlowSummaries belong to ONE BankAccount (ManyToOne)
    // Owning side - contains the foreign key 'account_id'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private BankAccount account;

    @Column(name = "summary_month", nullable = false)
    private LocalDate summaryMonth;

    @Column(name = "total_inflow", precision = 15, scale = 2)
    private BigDecimal totalInflow = BigDecimal.ZERO;

    @Column(name = "total_outflow", precision = 15, scale = 2)
    private BigDecimal totalOutflow = BigDecimal.ZERO;

    @Column(name = "net_flow", precision = 15, scale = 2)
    private BigDecimal netFlow = BigDecimal.ZERO;

    // Database manages this with: DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    // JPA won't insert/update it (insertable=false, updatable=false)
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}