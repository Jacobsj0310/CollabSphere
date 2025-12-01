package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Repository.ChatRoomRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserServiceImp1 implements UserService{

    private final UserRepository userRepository;


    public UserServiceImp1(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already in use");
        }
        // NOTE: password should be encoded in production (configure PasswordEncoder in SecurityConfig)
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User updates) {
        User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (updates.getUsername() != null && !updates.getUsername().isBlank()
                && !updates.getUsername().equals(existing.getUsername())) {
            if (userRepository.existsByUsername(updates.getUsername())) {
                throw new RuntimeException("Username already in use");
            }
            existing.setUsername(updates.getUsername());
        }
        if (updates.getEmail() != null && !updates.getEmail().isBlank()
                && !updates.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(updates.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            existing.setEmail(updates.getEmail());
        }
        if (updates.getPassword() != null && !updates.getPassword().isBlank()) {
            existing.setPassword(updates.getPassword()); // in prod encode
        }
        return userRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new RuntimeException("User not found");
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public User getUserById(Long id) {
        return null;
    }




    @Override
    public List<User> getAllUsers() {
        return List.of();
    }


}





