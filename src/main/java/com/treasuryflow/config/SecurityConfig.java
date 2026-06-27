package com.treasuryflow.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - Spring Security configuration for JWT-based stateless authentication
 *
 * SECURITY CONFIGURATION EXPLANATION:
 *
 * 1. @EnableWebSecurity - Enables Spring Security's web security support
 * 2. @EnableMethodSecurity - Enables @PreAuthorize, @PostAuthorize on methods
 *
 * KEY CONFIGURATIONS:
 *
 * A. CSRF PROTECTION - DISABLED (.csrf(csrf -> csrf.disable()))
 *    - JWT is stateless, no session = no CSRF risk
 *    - CSRF tokens are for session-based auth (form submissions)
 *    - REST APIs with JWT don't need CSRF protection
 *
 * B. SESSION MANAGEMENT - STATELESS (.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)))
 *    - No HTTP session created
 *    - No JSESSIONID cookie
 *    - Each request must carry JWT token
 *    - Scales horizontally (no session affinity needed)
 *
 * C. AUTHORIZATION RULES (.authorizeHttpRequests)
 *    - permitAll() - Public access (no auth required)
 *    - authenticated() - Requires valid JWT
 *    - hasRole("ADMIN") - Requires specific role
 *
 * D. PASSWORD ENCODING - BCrypt
 *    - BCryptPasswordEncoder with default strength (10 rounds)
 *    - Automatically salts passwords
 *    - Slow by design (prevents brute force)
 *
 * E. AUTHENTICATION PROVIDER - DaoAuthenticationProvider
 *    - Uses UserDetailsService to load user from DB
 *    - Uses PasswordEncoder to verify password
 *
 * F. JWT FILTER REGISTRATION
 *    - Adds JwtFilter BEFORE UsernamePasswordAuthenticationFilter
 *    - Ensures JWT is validated before standard auth processing
 *
 * G. CORS CONFIGURATION
 *    - Allows frontend (localhost:3000) to call API
 *    - Configures allowed origins, methods, headers, credentials
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Main security filter chain configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. DISABLE CSRF - JWT is stateless, no session to hijack
                .csrf(AbstractHttpConfigurer::disable)

                // 2. CONFIGURE CORS - Allow frontend origin
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. AUTHORIZATION RULES - Define which endpoints need auth
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**").permitAll()      // Register, login
                        .requestMatchers("/api/health").permitAll()       // Health check
                        .requestMatchers("/actuator/**").permitAll()      // Actuator endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // 4. STATELESS SESSION - No HTTP session, JWT only
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. SET AUTHENTICATION PROVIDER - Uses UserDetailsService + BCrypt
                .authenticationProvider(authenticationProvider())

                // 6. ADD JWT FILTER before Spring Security's username/password filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * DaoAuthenticationProvider - Bridges Spring Security with our UserDetailsService
     *
     * Flow when /api/auth/login is called:
     * 1. UsernamePasswordAuthenticationFilter intercepts login request
     * 2. Creates UsernamePasswordAuthenticationToken with credentials
     * 3. AuthenticationManager delegates to this provider
     * 4. Provider calls userDetailsService.loadUserByUsername(email)
     * 5. * 5. Provider uses passwordEncoder.matches(rawPassword, encodedPassword)
     * 6. Returns authenticated Authentication object if valid
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);    // Load user from DB
        provider.setPasswordEncoder(passwordEncoder());        // BCrypt password check
        return provider;
    }

    /**
     * AuthenticationManager - Required for programmatic authentication (login endpoint)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt Password Encoder - Industry standard for password hashing
     *
     * BCrypt features:
     * - Automatic salt generation (unique per password)
     * - Configurable cost factor (default 10 = 2^10 rounds)
     * - Slow hashing (prevents GPU brute force)
     * - Output includes algorithm, cost, salt, hash: $2a$10$salt$hash
     *
     * Example: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     *   $2a$ = BCrypt algorithm
     *   $10$ = cost factor (10 rounds)
     *   $N9qo8uLOickgx2ZMRZoMye = salt (22 chars)
     *   $IjZAgcfl7p92ldGxad68LJZdL17lhWy = hash
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Strength 10 (default)
    }

    /**
     * CORS Configuration - Allows React frontend (localhost:3000) to access API
     *
     * CORS (Cross-Origin Resource Sharing) Flow:
     * 1. Browser sends OPTIONS preflight request
     * 2. Server responds with allowed origins, methods, headers
     * 3. Browser allows actual request if origin matches
     *
     * Configuration options:
     * - allowedOrigins: Which domains can call API (frontend URL)
     * - allowedMethods: HTTP methods allowed (GET, POST, PUT, DELETE, OPTIONS)
     * - allowedHeaders: Request headers allowed (Authorization, Content-Type, etc.)
     * - allowCredentials: Allow cookies/auth headers (true for JWT)
     * - maxAge: Cache preflight response (3600s = 1 hour)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origin (React dev server)
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // Allow all standard HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers (including Authorization for JWT)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, Authorization header)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}