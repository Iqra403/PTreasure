package com.treasuryflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtFilter - Spring Security filter that intercepts every request to validate JWT token
 *
 * FILTER CHAIN POSITION:
 * This filter runs ONCE PER REQUEST (OncePerRequestFilter) before the Spring Security
 * filter chain. It extracts JWT from Authorization header, validates it, and if valid,
 * sets the Authentication in SecurityContextHolder so downstream filters/controllers
 * can access the authenticated user.
 *
 * JWT FLOW IN FILTER:
 *
 * 1. REQUEST COMES IN with header: Authorization: Bearer <token>
 *
 * 2. FILTER EXTRACTS TOKEN:
 *    - Checks for "Authorization" header
 *    - Verifies it starts with "Bearer "
 *    - Extracts token substring after "Bearer "
 *
 * 3. FILTER VALIDATES TOKEN:
 *    - Uses JwtUtil.validateToken(token) to check signature + expiration
 *    - If valid, extracts username from token
 *
 * 4. LOAD USER DETAILS:
 *    - Calls UserDetailsService.loadUserByUsername(username)
 *    - This hits the database to get user + roles
 *
 * 5. CREATE AUTHENTICATION OBJECT:
 *    - UsernamePasswordAuthenticationToken(userDetails, null, authorities)
 *    - null = no credentials needed (token already validated)
 *    - authorities = roles/permissions from UserDetails
 *
 * 6. SET SECURITY CONTEXT:
 *    - SecurityContextHolder.getContext().setAuthentication(authentication)
 *    - Now Spring Security knows user is authenticated
 *
 * 7. CHAIN CONTINUES:
 *    - filterChain.doFilter(request, response) passes to next filter
 *    - Controllers can now use @AuthenticationPrincipal or SecurityContext
 *
 * WHY OncePerRequestFilter?
 * - Guarantees filter runs exactly once per request dispatch
 * - Handles async dispatches, error dispatches correctly
 * - Prevents double-filtering in forward/include scenarios
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. Check if header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 3. Extract token (remove "Bearer " prefix - 7 chars)
            jwt = authHeader.substring(7);

            try {
                // 4. Extract username from token
                username = jwtUtil.getUsername(jwt);
            } catch (Exception e) {
                // Token parsing failed (malformed, expired, invalid signature)
                // Log error but continue - authentication will fail later
                logger.warn("JWT token parsing failed: " + e.getMessage());
            }
        }

        // 5. If username found AND no authentication already set in context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Load user details from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 7. Validate token against user details
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 8. Create authentication token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // Principal (user object)
                                null,                  // Credentials (not needed - token validated)
                                userDetails.getAuthorities()  // Authorities (roles)
                        );

                // 9. Set request details (IP, session info)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 10. SET AUTHENTICATION IN SECURITY CONTEXT
                // This is the key step - now Spring Security knows user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. Continue filter chain
        filterChain.doFilter(request, response);
    }
}