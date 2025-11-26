package com.CollabSphere.CollabSphere.Controller;

import com.CollabSphere.CollabSphere.DTO.AdminDTO;
import com.CollabSphere.CollabSphere.Entity.Admin;
import com.CollabSphere.CollabSphere.Service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/admin/{adminId}")
    public ResponseEntity<List<AdminDTO>> getLogByAdmin(@PathVariable Long adminId) {

        List<Admin> admins = adminService.getByAdmin(adminId);

        List<AdminDTO> adminDtos = admins.stream()
                .map(admin -> new AdminDTO(
                        admin.getId(),
                        admin.getUserId(),
                        admin.getEmail(),
                        admin.getRole(),
                        admin.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(adminDtos);
    }
}
