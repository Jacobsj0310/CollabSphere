package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Interface.JWTServiceInterface;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import com.CollabSphere.CollabSphere.Security.CustomUserDetailsService;
import com.CollabSphere.CollabSphere.Security.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JWTService implements JWTServiceInterface {

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository; // optional, sometimes useful

    @Override
    public String generateToken(com.CollabSphere.CollabSphere.Entity.User user) {
        return jwtUtil.generateToken(user);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public Optional<String> getUsername(String token) {
        String u = jwtUtil.getUserNameFromToken(token);
        return u == null || u.isBlank() ? Optional.empty() : Optional.of(u);
    }

    @Override
    public Optional<String> getRole(String token) {
        String r = jwtUtil.getRoleFromToken(token);
        return r == null || r.isBlank() ? Optional.empty() : Optional.of(r);
    }

    @Override
    public List<Long> getTeamIds(String token) {
        try {
            return jwtUtil.getTeamIdsFromToken(token);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public Authentication getAuthentication(String token) {
        if (token == null || !jwtUtil.validateToken(token)) return null;

        String username = jwtUtil.getUserNameFromToken(token);
        if (username == null || username.isBlank()) return null;

        // load UserDetails via your CustomUserDetailsService
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (Exception ex) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
