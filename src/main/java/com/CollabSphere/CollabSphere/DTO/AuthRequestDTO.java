package com.CollabSphere.CollabSphere.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jdk.jfr.Name;

public class AuthRequestDTO {

    private String name;

    @Email(message = "must be a valid email")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Email(message = "must be a valid email") @NotBlank(message = "email is required") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "must be a valid email") @NotBlank(message = "email is required") String email) {
        this.email = email;
    }

    public @NotBlank(message = "password is required") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "password is required") String password) {
        this.password = password;
    }



}
