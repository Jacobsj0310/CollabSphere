package com.CollabSphere.CollabSphere.Interface;


import com.CollabSphere.CollabSphere.DTO.UserDTO.UserRequest;
import com.CollabSphere.CollabSphere.DTO.UserDTO.UserResponse;

import java.util.List;

public interface UserServiceInterface {
    UserResponse createUser(UserRequest req);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    List<UserResponse> listUsers();
    UserResponse updateUser(Long id, UserRequest req, String requesterEmail);
    void deleteUser(Long id, String requesterEmail);
}
