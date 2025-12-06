package com.CollabSphere.CollabSphere.Security;

import com.CollabSphere.CollabSphere.Entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JWTUtil {

    private final String jwtSecret = "collabsphere_secret123456789";
    private final long jwtExpiration = 86400000L;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        String jcaAlg = SignatureAlgorithm.HS512.getJcaName();
        this.signingKey = new SecretKeySpec(keyBytes, jcaAlg);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRoleType().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String stripBearerPrefix(String token) {
        if (token == null) return null;
        String t = token.trim();
        if (t.toLowerCase().startsWith("bearer ")) return t.substring(7).trim();
        return t;
    }

    public String getUserNameFromToken(String token) {
        if (token == null || token.isBlank()) return null;
        token = stripBearerPrefix(token);
        try {
            return getClaims(token).getSubject();
        } catch (ExpiredJwtException ex) {
            Claims c = ex.getClaims();
            return c != null ? c.getSubject() : null;
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        if (token == null || token.isBlank()) return null;
        token = stripBearerPrefix(token);
        try {
            Object role = getClaims(token).get("role");
            return role != null ? role.toString() : null;
        } catch (ExpiredJwtException ex) {
            Object role = ex.getClaims().get("role");
            return role != null ? role.toString() : null;
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        token = stripBearerPrefix(token);
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Long> getTeamIdsFromToken(String token) {
        if (token == null || token.isBlank()) return Collections.emptyList();
        token = stripBearerPrefix(token);
        try {
            Object raw = getClaims(token).get("teams");
            if (raw == null) return Collections.emptyList();
            List<Long> out = new ArrayList<>();
            if (raw instanceof Collection) {
                for (Object item : (Collection<?>) raw) {
                    if (item instanceof Number) out.add(((Number) item).longValue());
                    else {
                        try { out.add(Long.parseLong(item.toString())); } catch (Exception ignored) {}
                    }
                }
            } else {
                try { out.add(Long.parseLong(raw.toString())); } catch (Exception ignored) {}
            }
            return out;
        } catch (ExpiredJwtException ex) {
            try {
                Object raw = ex.getClaims().get("teams");
                if (raw == null) return Collections.emptyList();
                List<Long> out = new ArrayList<>();
                if (raw instanceof Collection) {
                    for (Object item : (Collection<?>) raw) {
                        if (item instanceof Number) out.add(((Number) item).longValue());
                        else {
                            try { out.add(Long.parseLong(item.toString())); } catch (Exception ignored) {}
                        }
                    }
                } else {
                    try { out.add(Long.parseLong(raw.toString())); } catch (Exception ignored) {}
                }
                return out;
            } catch (Exception ignored) {
                return Collections.emptyList();
            }
        } catch (JwtException | IllegalArgumentException ex) {
            return Collections.emptyList();
        }
    }
}
