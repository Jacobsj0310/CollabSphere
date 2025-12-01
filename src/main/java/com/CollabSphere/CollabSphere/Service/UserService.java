package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import com.CollabSphere.CollabSphere.Entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User updates);
    void deleteUser(Long id);


    Optional<User> getById(Long id);
    List<User> getAll();

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    boolean existsById(Long id);


    User getUserById(Long id);

    List<User> getAllUsers();
}
