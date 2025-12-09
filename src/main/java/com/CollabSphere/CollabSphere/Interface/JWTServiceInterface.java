package com.CollabSphere.CollabSphere.Interface;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface JWTServiceInterface {

    /**
     * Generate a JWT for the given user (delegates to JWTUtil).
     */
    String generateToken(com.CollabSphere.CollabSphere.Entity.User user);

    /**
     * Validate the token (true if valid and not expired).
     */
    boolean validateToken(String token);

    /**
     * Extract username/email from token if available.
     */
    Optional<String> getUsername(String token);

    /**
     * Extract role (string) from token if available.
     */
    Optional<String> getRole(String token);

    /**
     * Extract list of team IDs encoded in the token (if any).
     */
    List<Long> getTeamIds(String token);

    /**
     * Create a Spring Security Authentication object for the given token.
     * Returns null if token invalid or user not found.
     */
    Authentication getAuthentication(String token);
}
