package com.treasuryflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BankAccount Entity - Maps to the 'bank_accounts' table in MySQL
 *
 * ANNOTATION EXPLANATIONS:
 *
 * @Entity - Marks this class as a JPA entity managed by the persistence context.
 *
 * @Table(name = "bank_accounts") - Maps to the 'bank_accounts' database table.
 *
 * @Data - Lombok: generates getters, setters, toString, equals, hashCode.
 *
 * @Id - Primary key field.
 *
 * @GeneratedValue(strategy = GenerationType.IDENTITY) - Database auto-increments the ID.
 *
 * @Column - Maps field to database column with constraints.
 *
 * @ManyToOne - Defines a many-to-one relationship.
 *              Many BankAccounts can belong to ONE User.
 *              This is the "owning side" of the relationship (has the foreign key).
 *
 * @JoinColumn - Specifies the foreign key column in THIS table (bank_accounts).
 *               name = "user_id" - the column name in bank_accounts table.
 *               referencedColumnName = "id" - the column in the referenced table (users).
 *               nullable = false - enforces NOT NULL on the foreign key.
 *               This creates: FOREIGN KEY (user_id) REFERENCES users(id)
 *
 * @Enumerated(EnumType.STRING) - Stores enum as STRING in database (e.g., 'CURRENT', 'SAVINGS')
 *                                instead of ordinal (0, 1, 2). More readable and safer.
 */
@Entity
@Table(name = "bank_accounts")
@Data
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Many BankAccounts belong to ONE User (ManyToOne)
    // This is the owning side - it has the foreign key column 'user_id'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "account_type", length = 30)
    private String accountType = "CURRENT";

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}