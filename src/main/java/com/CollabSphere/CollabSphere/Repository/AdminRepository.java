package com.CollabSphere.CollabSphere.Repository;

import com.CollabSphere.CollabSphere.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository <Admin, Long>{

    List<Admin>findByEmail(String email);
    Optional<Admin> findByUserId(Long Userid);
    List<Admin> findAllByRole(String role);
}
