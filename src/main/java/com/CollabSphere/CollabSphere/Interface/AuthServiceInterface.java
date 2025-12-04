package com.CollabSphere.CollabSphere.Interface;


import com.CollabSphere.CollabSphere.DTO.AuthRequestDTO;
import com.CollabSphere.CollabSphere.DTO.AuthResponseDTO;
import com.CollabSphere.CollabSphere.Entity.User;

import javax.security.sasl.AuthenticationException;
import java.util.Optional;

public interface AuthServiceInterface {
    // find user by email (required for login & JWT authentication)
    Optional<User> findByEmail(String email);

    // check email existence during registration
    boolean existsByEmail(String email);

    AuthResponseDTO register(AuthRequestDTO request);

    // LOGIN USER
    AuthResponseDTO login(AuthRequestDTO request) throws AuthenticationException;
}
