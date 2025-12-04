package com.CollabSphere.CollabSphere.Security;


import com.CollabSphere.CollabSphere.Entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JWTUtil {

    private final String jwtSecret = "collabsphere_secret123456789";
    private final long jwtExpiration = 86400000;

    private SecretKey signingKey;


    @PostConstruct
    public void init() {
        // Use SecretKeySpec to avoid Keys.hmacShaKeyFor dependency issues
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        String jcaAlg = SignatureAlgorithm.HS512.getJcaName(); // "HmacSHA512"
        this.signingKey = new SecretKeySpec(keyBytes, jcaAlg);
    }

    // Generate token
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRoleType().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    //getUserNameFromToken(String token)
    public String getUserNameFromToken(String token) {
        if (token == null || token.isBlank()) return null;
        token = stripBearerPrefix(token);

        try {
            Claims claims = getClaims(token);
            return claims.getSubject();
        } catch (ExpiredJwtException ex) {
            // Token expired — you can still read claims from the exception
            Claims c = ex.getClaims();
            return c != null ? c.getSubject() : null;
        } catch (JwtException | IllegalArgumentException ex) {
            // Invalid token
            return null;
        }
    }

    // Extract role if needed
    public String getRoleFromToken(String token) {
        if (token == null || token.isBlank()) return null;
        token = stripBearerPrefix(token);

        try {
            Claims claims = getClaims(token);
            Object role = claims.get("role");
            return role != null ? role.toString() : null;
        } catch (ExpiredJwtException ex) {
            Object role = ex.getClaims().get("role");
            return role != null ? role.toString() : null;
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    //validateToken(String token)
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        token = stripBearerPrefix(token);

        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // ---- Internal: parse claims using parserBuilder() and the SecretKey ----
    private Claims getClaims(String token) {
        // This will throw ExpiredJwtException, JwtException, or IllegalArgumentException when invalid
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Removes "Bearer " prefix if present (common when reading Authorization header)
    private String stripBearerPrefix(String token) {
        String t = token.trim();
        if (t.toLowerCase().startsWith("bearer ")) {
            return t.substring(7).trim();
        }
        return t;
    }

}
