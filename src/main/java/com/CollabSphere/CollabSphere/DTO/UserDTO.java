package com.CollabSphere.CollabSphere.DTO;

import com.CollabSphere.CollabSphere.Enum.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRequest {
        @NotBlank
        private String name;

        @NotBlank
        @Email
        private String email;

        /**
         * Plain password on input. Minimum length enforced.
         */
        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        /**
         * Optional: only admins should set this. If null on create -> default ROLE_MEMBER.
         */
        private RoleType roleType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private RoleType roleType;
    }
}