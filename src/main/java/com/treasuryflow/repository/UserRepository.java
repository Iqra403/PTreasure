package com.treasuryflow.repository;

import com.treasuryflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UserRepository - Spring Data JPA Repository for User entity
 *
 * SPRING DATA JPA REPOSITORY EXPLANATION:
 *
 * @Repository - Marks this interface as a Spring bean (repository component).
 *               Spring will auto-detect and register it during component scanning.
 *
 * JpaRepository<User, Long> - Extends JpaRepository with:
 *   - User: The entity type this repository manages
 *   - Long: The type of the entity's primary key (id field)
 *
 * By extending JpaRepository, you automatically get these methods WITHOUT writing any code:
 *
 * BASIC CRUD OPERATIONS:
 * - save(User)           - INSERT or UPDATE (uses @Id to determine if new or existing)
 * - findById(Long)       - SELECT * FROM users WHERE id = ?
 * - findAll()            - SELECT * FROM users
 * - existsById(Long)     - SELECT COUNT(*) > 0 FROM users WHERE id = ?
 * - count()              - SELECT COUNT(*) FROM users
 * - deleteById(Long)     - DELETE FROM users WHERE id = ?
 * - delete(User)         - DELETE FROM users WHERE id = ?
 * - deleteAll()          - DELETE FROM users
 *
 * QUERY DERIVATION (Spring Data JPA parses method names to generate SQL):
 * - findByEmail(String email)      → SELECT * FROM users WHERE email = ?
 * - findByNameContaining(String)   → SELECT * FROM users WHERE name LIKE %?%
 * - findByRole(String role)        → SELECT * FROM users WHERE role = ?
 * - findByEmailIgnoreCase(String)  → SELECT * FROM users WHERE LOWER(email) = LOWER(?)
 * - findTop10ByOrderByCreatedAtDesc() → SELECT * FROM users ORDER BY created_at DESC LIMIT 10
 *
 * NAMING CONVENTION FOR QUERY METHODS:
 * findBy<PropertyName>                    - Exact match (=)
 * findBy<PropertyName>Containing          - LIKE %value%
 * findBy<PropertyName>StartingWith        - LIKE value%
 * findBy<PropertyName>EndingWith          - LIKE %value
 * findBy<PropertyName>IgnoreCase          - Case-insensitive comparison
 * findBy<PropertyName>GreaterThan         - > comparison
 * findBy<PropertyName>LessThan            - < comparison
 * findBy<PropertyName>Between             - BETWEEN ? AND ?
 * findBy<PropertyName>In(Collection)      - IN (?, ?, ?)
 * findBy<PropertyName>IsNull              - IS NULL
 * findBy<PropertyName>IsNotNull           - IS NOT NULL
 * findBy<PropertyName1>And<PropertyName2> - Multiple conditions (AND)
 * findBy<PropertyName1>Or<PropertyName2>  - Multiple conditions (OR)
 * OrderBy<PropertyName>Asc/Desc           - ORDER BY clause
 * Top/First<N>                            - LIMIT clause
 *
 * For complex queries beyond naming conventions, use @Query annotation with JPQL or native SQL.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA automatically converts this method name to:
    // SELECT * FROM users WHERE email = ?1
    // ?1 is the first parameter (email)
    User findByEmail(String email);

    // Boolean existsByEmail(String email) → SELECT COUNT(*) > 0 FROM users WHERE email = ?
    // Useful for checking if email already exists before registration
    boolean existsByEmail(String email);

    // findByNameContaining(String name) → SELECT * FROM users WHERE name LIKE %?1%
    // Case-insensitive search for partial name matches
    // List<User> findByNameContainingIgnoreCase(String name);
}