package com.CollabSphere.CollabSphere.Repository;

import com.CollabSphere.CollabSphere.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    // find user by email (required for login & JWT authentication)
    Optional<User> findByEmail(String email);

    // check email existence during registration
    boolean existsByEmail(String email);
}
