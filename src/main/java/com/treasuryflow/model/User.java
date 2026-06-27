package com.treasuryflow.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * User Entity - Maps to the 'users' table in MySQL
 *
 * ANNOTATION EXPLANATIONS:
 *
 * @Entity - Marks this class as a JPA entity (a persistent class mapped to a database table).
 *           JPA will manage the lifecycle of instances of this class.
 *
 * @Table(name = "users") - Specifies the database table name this entity maps to.
 *           Without this, JPA would use the class name (User -> user table).
 *
 * @Data - Lombok annotation that generates getters, setters, toString(), equals(), and hashCode().
 *         Reduces boilerplate code significantly.
 *
 * @Id - Marks the field as the primary key of the entity.
 *
 * @GeneratedValue(strategy = GenerationType.IDENTITY) - Configures how the primary key is generated.
 *           IDENTITY means the database auto-increments the value (AUTO_INCREMENT in MySQL).
 *           The database assigns the ID upon INSERT.
 *
 * @Column - Defines the mapping between a field and a database column.
 *           name = "column_name" - specifies the actual column name in the database.
 *           nullable = false - adds NOT NULL constraint.
 *           unique = true - adds UNIQUE constraint.
 *           length = 100 - specifies VARCHAR length.
 *
 * @JsonIgnore - Jackson annotation to exclude this field from JSON serialization.
 *               Prevents the password from being sent in API responses (security best practice).
 */
@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    @JsonIgnore  // Excludes password from JSON responses for security
    private String password;

    @Column(name = "role", length = 20)
    private String role = "USER";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}