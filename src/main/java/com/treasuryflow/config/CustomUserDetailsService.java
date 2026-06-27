package com.treasuryflow.config;

import com.treasuryflow.model.User;
import com.treasuryflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService - Loads user from database for Spring Security authentication
 *
 * FLOW:
 * 1. User submits login credentials
 * 2. AuthenticationManager calls loadUserByUsername(email)
 * 3. This service queries database via UserRepository
 * 4. Returns UserDetails with encoded password
 * 5. Spring Security compares passwords using PasswordEncoder
 *
 * UserDetails interface provides:
 * - getUsername() → email (used as principal)
 * - getPassword() → BCrypt encoded password from DB
 * - getAuthorities() → roles/permissions (ROLE_USER, ROLE_ADMIN)
 * - isAccountNonExpired(), isAccountNonLocked(), isCredentialsNonExpired(), isEnabled()
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user by email (username in Spring Security terms)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert our User entity to Spring Security UserDetails
        // Using org.springframework.security.core.userdetails.User builder
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())  // Already BCrypt encoded
                .roles(user.getRole().replace("ROLE_", ""))  // Remove ROLE_ prefix if present
                .build();
    }
}