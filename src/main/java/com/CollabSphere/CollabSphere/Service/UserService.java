package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.DTO.UserDTO.UserRequest;
import com.CollabSphere.CollabSphere.DTO.UserDTO.UserResponse;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Enum.RoleType;
import com.CollabSphere.CollabSphere.Interface.UserServiceInterface;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserServiceInterface {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private UserResponse toDto(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .roleType(u.getRoleType())
                .build();
    }

    @Override
    public UserResponse createUser(UserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        RoleType role = (req.getRoleType() != null) ? req.getRoleType() : RoleType.MEMBER;

        User u = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roleType(role)
                .build();

        User saved = userRepository.save(u);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toDto(u);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User u = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toDto(u);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest req, String requesterEmail) {
        User target = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        boolean isAdmin = requester.getRoleType() == RoleType.ADMIN;
        boolean isSelf = requester.getId().equals(target.getId());

        if (!isAdmin && !isSelf) {
            throw new SecurityException("Not authorized to update this user");
        }

        // update fields
        if (req.getName() != null && !req.getName().isBlank()) target.setName(req.getName());
        if (req.getPassword() != null && !req.getPassword().isBlank()) target.setPassword(passwordEncoder.encode(req.getPassword()));

        // Only admin may change role
        if (req.getRoleType() != null && isAdmin) {
            target.setRoleType(req.getRoleType());
        }

        User saved = userRepository.save(target);
        return toDto(saved);
    }

    @Override
    public void deleteUser(Long id, String requesterEmail) {
        User target = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        boolean isAdmin = requester.getRoleType() == RoleType.ADMIN;
        boolean isSelf = requester.getId().equals(target.getId());

        if (!isAdmin && !isSelf) {
            throw new SecurityException("Not authorized to delete this user");
        }

        userRepository.delete(target);
    }
}
