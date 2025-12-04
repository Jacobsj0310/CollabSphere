package com.CollabSphere.CollabSphere.DTO;

import com.CollabSphere.CollabSphere.Enum.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDTO {

    private String name;
    private String email;
    private Role role;
    private String token;

    public AuthResponseDTO(String name, String email, RoleType roleType, String token) {
    }
}
