package com.CollabSphere.CollabSphere.Security;

import com.CollabSphere.CollabSphere.Repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring will inject the repository
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email is empty");
        }

        com.CollabSphere.CollabSphere.Entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Map Role enum to a Spring Security authority (ROLE_ADMIN / ROLE_MEMBER)
        String roleName = (user.getRoleType() != null) ? "ROLE_" + user.getRoleType().name() : "ROLE_MEMBER";
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        // Build and return Spring Security User (username = email)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())   // must be encoded (BCrypt) in DB
                .authorities(List.of(authority))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
