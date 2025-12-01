package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Repository.ChatRoomRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserServiceImp1 implements UserService{


        private UserRepository userRepository;

        public void UserServiceImpl(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        public User createUser(User user) {

            // simple duplicate check
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("Email already used");
            }
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new RuntimeException("Username already used");
            }

            return userRepository.save(user);
        }

        @Override
        public User updateUser(Long id, User user) {
            User existing = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User Not Found"));

            if (user.getUsername() != null) {
                existing.setUsername(user.getUsername());
            }

            if (user.getEmail() != null) {
                existing.setEmail(user.getEmail());
            }

            if (user.getPassword() != null) {
                existing.setPassword(user.getPassword());
            }

            return userRepository.save(existing);
        }

        @Override
        public void deleteUser(Long id) {
            if (!userRepository.existsById(id)) {
                throw new RuntimeException("User Not Found");
            }
            userRepository.deleteById(id);
        }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<User> getAll() {
        return List.of();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
        public User getUserById(Long id) {
            return userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User Not Found"));
        }

        @Override
        public List<User> getAllUsers() {
            return userRepository.findAll();
        }




}





