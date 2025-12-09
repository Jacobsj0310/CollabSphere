package com.CollabSphere.CollabSphere.Controller;

import com.CollabSphere.CollabSphere.DTO.UserDTO.UserRequest;
import com.CollabSphere.CollabSphere.DTO.UserDTO.UserResponse;
import com.CollabSphere.CollabSphere.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Create new user (self-register). Admin can create user with a role by passing roleType in request.
     */
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest req, Authentication authentication) {
        // If an authenticated admin creates a user and supplies roleType, allow it.
        // Otherwise, create as MEMBER.
        UserResponse created = userService.createUser(req);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id, Authentication authentication) {
        // Authorization handled in service
        UserResponse resp = userService.getUserById(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String email = authentication.getName();
        UserResponse resp = userService.getUserByEmail(email);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers(Authentication authentication) {
        // Optionally restrict listing to admins at controller-level; service currently returns all
        List<UserResponse> list = userService.listUsers();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UserRequest req,
                                                   Authentication authentication) {
        String requester = authentication.getName();
        UserResponse updated = userService.updateUser(id, req, requester);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        String requester = authentication.getName();
        userService.deleteUser(id, requester);
        return ResponseEntity.noContent().build();
    }
}