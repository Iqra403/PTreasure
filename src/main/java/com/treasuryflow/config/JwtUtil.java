package com.treasuryflow.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil - Utility class for JWT (JSON Web Token) operations
 *
 * JWT FLOW EXPLANATION:
 *
 * 1. TOKEN GENERATION (generateToken):
 *    - Input: username (subject), optional claims (roles, permissions)
 *    - Process:
 *      a. Create claims map with subject (username) and any additional claims
 *      b. Set issuedAt (now) and expiration (now + expirationMs)
 *      c. Sign with HS256 algorithm using secret key
 *      d. Encode as compact string: header.payload.signature
 *    - Output: String token (e.g., "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
 *
 * 2. TOKEN VALIDATION (validateToken):
 *    - Input: token string, UserDetails (from Spring Security)
 *    - Process:
 *      a. Parse token and extract claims
 *      b. Extract username (subject) from claims
 *      c. Check if username matches UserDetails username
 *      d. Check if token is not expired
 *    - Output: true if valid, false otherwise
 *
 * 3. CLAIMS EXTRACTION (getUsername, getClaimFromToken):
 *    - Parse token using secret key
 *    - Extract specific claim (subject, expiration, custom claims)
 *    - Uses Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
 *
 * JWT STRUCTURE (3 parts separated by dots):
 * - HEADER: {"alg":"HS256","typ":"JWT"} (base64url encoded)
 * - PAYLOAD: {"sub":"user@example.com","iat":1234567890,"exp":1234571490,"roles":["USER"]}
 * - SIGNATURE: HMAC-SHA256(base64url(header) + "." + base64url(payload), secret)
 *
 * SECURITY NOTES:
 * - Secret key must be at least 256 bits (32 bytes) for HS256
 * - Store secret in environment variable, NOT in code
 * - Use short expiration (15-30 min) for access tokens
 * - Consider refresh tokens for longer sessions
 * - Always use HTTPS in production
 */
@Component
public class JwtUtil {

    // Secret key for signing tokens - MUST be at least 256 bits (32 chars) for HS256
    // In production, load from environment variable: ${JWT_SECRET:your-secret-key}
    @Value("${jwt.secret:mySuperSecretKeyThatIsAtLeast32CharactersLongForHS256Algorithm}")
    private String secret;

    // Token expiration in milliseconds (24 hours = 86400000 ms)
    // For production access tokens, use 15-30 minutes (900000-1800000 ms)
    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    /**
     * Generate JWT token for a username
     *
     * @param username the subject (typically email) to embed in token
     * @return signed JWT token string
     */
    public String generateToken(String username) {
        // Create claims map - payload data
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);  // Standard claim: subject
        claims.put("iat", new Date()); // Issued at (optional, JJWT adds automatically)

        return createToken(claims, username);
    }

    /**
     * Generate JWT token with custom claims (e.g., roles, permissions)
     *
     * @param claims custom claims to include in token payload
     * @param username subject of the token
     * @return signed JWT token string
     */
    public String generateToken(Map<String, Object> claims, String username) {
        return createToken(claims, username);
    }

    /**
     * Internal method to build and sign the JWT
     */
    private String createToken(Map<String, Object> claims, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)              // Set custom claims (payload)
                .setSubject(username)           // Standard "sub" claim
                .setIssuedAt(now)               // "iat" claim - issued at
                .setExpiration(expiry)          // "exp" claim - expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Sign with HS256
                .compact();                     // Encode to compact string (header.payload.signature)
    }

    /**
     * Extract username (subject) from token
     *
     * @param token JWT token string
     * @return username from "sub" claim
     */
    public String getUsername(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token
     */
    public Date getExpirationDate(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from token
     *
     * @param token JWT token string
     * @param claimsResolver function to extract specific claim from Claims object
     * @return claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse token and extract all claims
     * This validates the signature automatically - throws exception if invalid
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Verify signature with same key
                .build()
                .parseClaimsJws(token)           // Parse and validate
                .getBody();                      // Get payload (claims)
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDate(token);
        return expiration.before(new Date());
    }

    /**
     * Validate token against UserDetails (from Spring Security)
     *
     * @param token JWT token from request
     * @param userDetails UserDetails loaded by UserDetailsService
     * @return true if token is valid for this user and not expired
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsername(token);
            // Valid if: username matches AND token not expired
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            // Any parsing/validation error means token is invalid
            return false;
        }
    }

    /**
     * Validate token without UserDetails (just checks signature and expiration)
     */
    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get signing key from secret string
     * Decodes base64 secret to SecretKey for HS256 algorithm
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}