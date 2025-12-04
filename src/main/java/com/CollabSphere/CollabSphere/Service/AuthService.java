package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Enum.RoleType;
import com.CollabSphere.CollabSphere.DTO.AuthRequestDTO;
import com.CollabSphere.CollabSphere.DTO.AuthResponseDTO;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Interface.AuthServiceInterface;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import com.CollabSphere.CollabSphere.Security.JWTUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceInterface {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public AuthResponseDTO register(AuthRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleType(RoleType.MEMBER)   // default role
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser);

        return new AuthResponseDTO(
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRoleType(),
                token
        );
    }

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {

        // This will throw an exception automatically if credentials are invalid
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(user);

        return new AuthResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getRoleType(),
                token
        );
    }
}
