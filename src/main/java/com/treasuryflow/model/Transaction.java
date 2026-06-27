package com.treasuryflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction Entity - Maps to the 'transactions' table in MySQL
 *
 * ANNOTATION EXPLANATIONS:
 *
 * @Entity - JPA entity managed by persistence context.
 *
 * @Table(name = "transactions") - Maps to 'transactions' database table.
 *
 * @Data - Lombok boilerplate reduction.
 *
 * @Id + @GeneratedValue(IDENTITY) - Auto-increment primary key.
 *
 * @ManyToOne - Many Transactions belong to ONE BankAccount.
 *              This is the owning side (has the foreign key 'account_id').
 *
 * @JoinColumn - Defines the foreign key column in THIS table (transactions).
 *               name = "account_id" - column in transactions table.
 *               referencedColumnName = "id" - column in bank_accounts table.
 *               Creates: FOREIGN KEY (account_id) REFERENCES bank_accounts(id)
 *
 * @Enumerated(EnumType.STRING) - Stores enum as STRING ('CREDIT' or 'DEBIT')
 *                                in the database instead of ordinal numbers.
 *                                Required for MySQL ENUM compatibility.
 *
 * @Column(precision = 15, scale = 2) - For DECIMAL(15,2) columns.
 *              precision = total digits (15), scale = digits after decimal (2).
 *
 * @Temporal(TemporalType.DATE) - Maps java.time.LocalDate to DATE (not DATETIME).
 *              Alternative: @Column(columnDefinition = "DATE") for LocalDate.
 *
 * fetch = FetchType.LAZY - Loads the related BankAccount only when accessed.
 *              Improves performance by avoiding unnecessary joins.
 *              Use FetchType.EAGER if you always need the account with transaction.
 */
@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Many Transactions belong to ONE BankAccount (ManyToOne)
    // Owning side - contains the foreign key 'account_id'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private BankAccount account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Transaction Type Enum - Maps to MySQL ENUM('CREDIT','DEBIT')
     * Using STRING storage via @Enumerated(EnumType.STRING)
     */
    public enum TransactionType {
        CREDIT,
        DEBIT
    }
}