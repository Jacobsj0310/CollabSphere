package com.CollabSphere.CollabSphere.Service;


import com.CollabSphere.CollabSphere.DTO.AdminDTO;
import com.CollabSphere.CollabSphere.Entity.Admin;
import com.CollabSphere.CollabSphere.Repository.AdminRepository;
/*import jakarta.transaction.Transactional;*/
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional

public class AdminService{
    private final AdminRepository adminRepository;

    // Explicit constructor injection (works even if Lombok/annotation processing is not set up)
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin createAdmin(AdminDTO dto) {
        Optional<Admin> existing = adminRepository.findByUserId(dto.getUserId());
        if (existing.isPresent()) {
            return existing.get();
        }

        Admin admin = new Admin();

        if (dto.getId() != null) {
            admin.setId(dto.getId());
        }

        admin.setUserId(dto.getUserId());
        admin.setEmail(dto.getEmail());
        admin.setRole(dto.getRole());

        return adminRepository.save(admin);
    }

    public Admin createAdmin(Long userId, String email) {
        Optional<Admin> existing = adminRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Admin admin = new Admin();
        admin.setUserId(userId);
        admin.setEmail(email);

        return adminRepository.save(admin);
    }

    @Transactional(readOnly = true)
    public Admin findById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(Long userId) {
        return adminRepository.findByUserId(userId).isPresent();
    }

    public void deleteById(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new IllegalArgumentException("Admin not found with id: " + id);
        }
        adminRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Admin> getByAdmin(Long adminId) {
        Optional<Admin> adminOpt = adminRepository.findByUserId(adminId);
        return adminOpt.map(List::of).orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public List<Admin> listAllAdmins() {
        return adminRepository.findAllByRole("ADMIN");
    }
}

